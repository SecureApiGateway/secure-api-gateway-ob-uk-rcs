# Secure API Gateway - Remote Consent Service (RCS)

## Prerequisites

- Kubernetes v1.23 +
- Helm 3.0.0 +

## Helm Charts
### Deployment
The deployment of RCS is a deployment and service. It should only be installed as part of the secure-api-gateway umbarella chart and not standalone.  The deployment will require a docker image which needs to be built via the [Makefile](https://github.com/SecureApiGateway/secure-api-gateway-ob-uk-rcs/blob/master/Makefile) before trying to deploy to kubernetes 

NOTE: There is no repo or image specified in the Values.yaml - This needs to be done in a seperate 'deployments' repo using an values.yaml overlay - No other values are required to be overwritten in the overlay but can be if needs be.

### Example Manifest
This is an example manifest using the values.yaml file provided, there is no overlay values in this generated manifest hence why there is no repo URL in spec.template.spec.containers.0.image

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
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| deployment.apiVersion| string | apps/v1 | Version of the Kubernetes API to use |
| deployment.containerPort | integer | 8080 | Container port exposed by a pod or deployment |
| deployment.image.repo | string | {} | Repo to pull images from - Value should exist in values.yaml overlay in deployment repo |
| deployment.image.tag | string | {} | Tag to deploy - Value should exist in values.yaml overlay in deployment repo |
| deployment.image.imagePullPolicy | string | Always | Policy for pulling images
| deployment.java.opts | string | -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=50 -agentlib:jdwp=transport=dt_socket,address=*:9091,server=y,suspend=n | Additional Java config
