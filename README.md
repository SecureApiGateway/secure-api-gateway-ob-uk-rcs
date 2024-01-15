## Secure API Gateway - Remote Consent Service (RCS)

A multi-module maven project providing a UK Open Banking Remote Consent Service.

### Setting up Maven (developers)

Download and install Maven settings.xml file by running the command below and substituting in your backstage username
and password.

```bash
curl -u $BACKSTAGE_USERNAME http://maven.forgerock.org/repo/private-releases/settings.xml > ~/.m2/settings.xml
```

### Build the project
#### Linux dependencies
Certain distributions of Linux no longer come with libssl1.1 installed as standard e.g. Ubuntu 22.04

libssl1.1 is required to run embedded MongoDB when running the unit tests (it is not required by the RS in production)

The following command installs the library manually:
```bash
libssl_hash="0b3251aee55db6e20d02f4b9a2b703c9874a85ab6a20b12f4870f52f91633d37"
wget http://security.ubuntu.com/ubuntu/pool/main/o/openssl/libssl1.1_1.1.1f-1ubuntu2.20_amd64.deb
 
local_file_hash=$(sha256sum "libssl1.1_1.1.1f-1ubuntu2.20_amd64.deb" | awk '{print $1}')
if [[ "$local_file_hash" != "$libssl_hash" ]]; then
        echo "Checksum verification failed"
else
    echo "Checksum verification passed"
    sudo dpkg -i libssl1.1_1.1.1f-1ubuntu2.20_amd64.deb
fi
```
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
| consent.repo.uri                          | Base URI of the Consent Repo API                                    |
| rs.api.uri                                | Base URI of the RS API                                              |
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
