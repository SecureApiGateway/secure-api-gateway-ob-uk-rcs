## SBA Toolkit's UK Open Banking RCS

A multi-module maven project providing a UK Open Banking Remote Consent Service for the Secure Banking Accelerator
Toolkit.

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

### How to run

**Run docker compose**
> Config server profile properties location from local volume (`securebanking-openbanking-uk-rcs-sample/docker/config`)
```shell
./securebanking-openbanking-uk-rcs-sample/docker/run-docker-compose-local.sh
```

### Supported APIs

Upon starting the application, a list of supported APIs can be obtained dynamically from the Swagger Specification
Endpoint:

- Swagger Json descriptor: `http://<host>:<port>/api-docs`
- Swagger UI documentation: `http://<host>:<port>/swagger-ui/`
> Substitute `<host>` and `<port` as necessary
