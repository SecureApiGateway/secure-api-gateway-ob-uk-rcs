# Secure API Gateway - Remote Consent Service (RCS)

## Prerequisites

- Kubernetes v1.23 +
- Helm 3.0.0 +

To add the forgerock helm artifactory repository to your local machine to consume helm charts use the following
```console
  helm repo add forgerock-helm https://maven.forgerock.org/artifactory/forgerock-helm-virtual/ --username [backstage_username]  --password [backstage_password]
  helm repo update

```
NOTE: You must have a valid [subscription](https://backstage.forgerock.com/knowledge/kb/article/a57648047#XAYQfS) to aquire the `backstage_username` and `backstage_password` values.

## Helm Charts
### Deployment
RCS should only be installed as part of the [secure-api-gateway umbarella chart](https://github.com/SecureApiGateway/secure-api-gateway-releases/secure-api-gateway) and not standalone from this repositry.  

However, as part of the deployment of the secure-api-gateway, you must build the java artifacts and built the docker image via the [Makefile](https://github.com/SecureApiGateway/secure-api-gateway-ob-uk-rcs/blob/master/Makefile). 

Only once this has been done for all the components can the [steps to deploy](https://github.com/SecureApiGateway/secure-api-gateway-releases/secure-api-gateway/readme.md) be performed to deploy the secure-api-gateway

### Example Manifest
This is an example manifest using the values.yaml file provided, there is no overlay values in this generated manifest hence why there is no repo URL in `spec.template.spec.containers.0.image`

```yaml
---
# Source: securebanking-openbanking-uk-rcs/templates/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: remote-consent-service
  labels:
    app: remote-consent-service
spec:
  type: ClusterIP
  ports:
  - name: remote-consent-service
    port: 8080
    targetPort: 80
    protocol: TCP
  selector:
    app: remote-consent-service
---
# Source: securebanking-openbanking-uk-rcs/templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: remote-consent-service
  labels:
    app: remote-consent-service
spec:
  replicas: 1
  strategy:
    type: RollingUpdate
    
    rollingUpdate:
      maxSurge: 50%
      maxUnavailable: 25%
    
  selector:
    matchLabels:
      app: remote-consent-service
  template:
    metadata:
      labels:
        app: remote-consent-service
        appVersion: 1.0.0
        helmVersion: 1.0.0
    spec:
      containers:
        - name: remote-consent-service
          image: ":1.0.0"
          imagePullPolicy: Always
          volumeMounts:
            - name: rcs-signing
              mountPath: /app/secrets
              readOnly: true
          ports:
            - name: http-server
              containerPort: 8080
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            periodSeconds: 5
            failureThreshold: 3
            successThreshold: 1
            timeoutSeconds: 5
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 120
            periodSeconds: 5
            failureThreshold: 5
            successThreshold: 1
            timeoutSeconds: 5
          env:
          - name: CONSENT_REPO_URI
            valueFrom:
              configMapKeyRef:
                name: deployment-config
                key: CONSENT_REPO_URI
          - name: RS_API_URI
            valueFrom:
              configMapKeyRef:
                name: deployment-config
                key: RS_API_URI
          - name: SERVER_PORT
            value: "8080"
          - name: SPRING_PROFILES_ACTIVE
            value: "docker"
          - name: JAVA_OPTS
            value: -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=50 -agentlib:jdwp=transport=dt_socket,address=*:9091,server=y,suspend=n
          - name: RCS_CONSENT_RESPONSE_JWT_PRIVATEKEYPATH
            value: /app/secrets/rcs-signing.key
          - name: RCS_CONSENT_RESPONSE_JWT_SIGNINGKEYID
            valueFrom:
              configMapKeyRef:
                name: deployment-config
                key: RCS_CONSENT_RESPONSE_JWT_SIGNINGKEYID
          - name: RCS_CONSENT_RESPONSE_JWT_ISSUER
            valueFrom:
              configMapKeyRef:
                name: deployment-config
                key: RCS_CONSENT_RESPONSE_JWT_ISSUER
          resources:
            limits:
              cpu: 0.5
              memory: 512Mi
            requests:
              cpu: 0.25
              memory: 256Mi
      volumes:
        - name: rcs-signing
          secret:
            secretName: rcs-signing
            optional: false
```
## Env Config
| Key | Default | Description | Source |
|-----|---------|-------------|--------|
| CONSENT_REPO_URI | http://ig:80 | URI of IG | deployment-config |
|RS_API_URI | http://test-facility-bank:8080 | URI of Test Facility Bank | deployment-config |
| SERVER_PORT | 8080 | What port does the container use |deployment.containerPort |
| SPRING_PROFILES_ACTIVE | docker |What spring provile to use | Hardcoded | 
| RCS_CONSENT_RESPONSE_JWT_PRIVATEKEYPATH | /app/secrets/rcs-signing.key | Where to find the private signing key | Hardcoded |
| RCS_CONSENT_RESPONSE_JWT_SIGNINGKEYID | rcs-jwt-signer | What is the signing key ID | deployment-config |
| RCS_CONSENT_RESPONSE_JWT_ISSUER | secure-open-banking-rcs | What is the JWT responce issuer | deployment-config |

## Values
| Key | Type | Description | Default |
|-----|------|-------------|---------|
| deployment.apiVersion| string | Version of the Kubernetes API to use | apps/v1 |
| deployment.containerPort | integer | Container port exposed by a pod or deployment | 8080 |
| deployment.image.repo | string | Repo to pull images from - Value should exist in values.yaml overlay in deployment repo | {} |
| deployment.image.tag | string | Tag to deploy - Value should exist in values.yaml overlay in deployment repo | {} |
| deployment.image.imagePullPolicy | string | Policy for pulling images | Always |
| deployment.java.opts | string | Additional Java config | -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=50 -agentlib:jdwp=transport=dt_socket,address=*:9091,server=y,suspend=n |
| deployment.livenessProbe.initialDelaySeconds | integer | Time to wait until liveness probe beings | 120 |
| deployment.livenessProbe.periodSeconds | integer | How long to test the probe | 5 |
| deployment.livenessProbe.failureThreshold | integer | How many times the probe can fail before declaring the pod unhealthy | 5 |
| deployment.livenessProbe.successThreshold | integer | How many times the prob must succeed before declaring the pod healthy | 1 |
| deployment.livenessProbe.timeoutSeconds | integer | Amount of time the probe will try hit the endpoint before declaring  5 |unsuccessful |
| deployment.readinessProbe.periodSeconds | integer | How long to test the probe | 5 |
| deployment.readinessProbe.failureThreshold | integer | How many times the probe can fail before declaring the pod unhealthy | 3 |
| deployment.readinessProbe.successThreshold | integer| How many times the prob must succeed before declaring the pod healthy | | 1 
| deployment.readinessProbe.timeoutSeconds | integer | Amount of time the probe will try hit the endpoint before declaring unsuccessful | 5 |
| deployment.resources.limits.cpu | integer | Max amount of CPU the pod can consume | 0.5 |
| deployment.resources.limits.memory | string | Max amount of memory the pod can consume | 512Mi |
| deployment.resources.requests.cpu | integer | Minimum requested CPU required to run the pod | 0.25 |
| deployment.resources.requests.memory | string | Minimum requested memory required to run the pod | 256Mi |
| deployment.rollingUpdate.maxSurge | string | The maximum number of pods that can be scheduled above the desired number of pods | 50% |
| deployment.rollingUpdate.maxUnavailable | string | The maximum number of pods that can be unavailable during the update | 25% |
| deployment.starategyType | string | Type of deployment | RollingUpdate |
| service.apiVersion | string | Version of the Kubernetes API to use | v1 |
| service.port | integer | Container port exposed by a pod or deployment | 8080 |
| service.protocol | string | Protocol the service will use | TCP |
| service.targetPort | integer | Host Machine port that traffic is diverted too | 80 | 
| service.type | string | Type of service to create | ClusterIP |

NOTE: There is no `deployment.image.repo` or `deployment.image.tag` specified in the `Values.yaml` - This needs to be done in a seperate 'deployments' repo using an values.yaml overlay. You may overwrite any of the other values if required. 
