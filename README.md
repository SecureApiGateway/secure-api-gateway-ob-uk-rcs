## Secure API Gateway - Remote Consent Service (RCS)

A multi-module maven project providing a UK Open Banking Remote Consent Service.

### Setting up Maven (developers)

Download and install Maven settings.xml file by running the command below and substituting in your backstage username
and password.

```bash
curl -u $BACKSTAGE_USERNAME http://maven.forgerock.org/repo/private-releases/settings.xml > ~/.m2/settings.xml
```

### Build the project

#### Maven build

From the command line, simply run:

```bash
mvn clean install
```

This will run any JUnit/Spring integration tests and build the required JAR file and docker image.

### Spring config
This module is built using Spring Boot and makes use of Spring Properties driven configuration.

`secure-api-gateway-ob-uk-rcs-server` module contains the Spring configuration, see: [secure-api-gateway-ob-uk-rcs-server/src/main/resources/application.yml](secure-api-gateway-ob-uk-rcs-server/src/main/resources/application.yml).

#### Deployment Specific Config
The Spring config contains sensible defaults for configuration properties, when there is no sensible default then the property is left undefined and the application will fail with an exception at startup. 

In this section we will discuss the config that is deployment specific, this config needs to be provided in order to run the application.


| Property                                  | Description                                                         |
|-------------------------------------------|---------------------------------------------------------------------|
| consent.repo.host                         | The host that is running the Consent Repo                           |
| rs.internal.svc                           | The internal service address of the Resource Server (RS)            |
| rcs.consent.response.jwt.signingKeyId     | kid of the key used to sign JWTs produced by the application        |
| rcs.consent.response.jwt.privateKeyPath   | Path to the RSA private key PEM file that is used to sign the JWTs  |
| rcs.consent.response.jwt.issuer           | Value to set as the `iss` claim in JWTs produced by the application |
| rcs.consent.response.jwt.signingAlgorithm | Signing algorithm used to compute JWT signatures. Default: PS256    |

The recommended way to supply these values is by using OS Environment Variables.


### How to run

**Run docker compose**
> Config server profile properties location from local volume (`securebanking-openbanking-uk-rcs-server/docker/config`)
```shell
./securebanking-openbanking-uk-rcs-server/docker/run-docker-compose-local.sh
```
