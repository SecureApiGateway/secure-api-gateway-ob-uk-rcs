## SBA Toolkit's UK Open Banking RCS

A multi-module maven project providing a UK Open Banking Remote Consent Service for the Secure Banking Accelerator
Toolkit. 

### Setting up Maven

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

Either run the docker image created in the previous step, or run the project's SpringBoot application class:

```com.forgerock.securebanking.openbanking.uk.rcs.RcsApplication```

Note that the application has a dependency on MongoDB and will not start up without it. If running locally, this can be
achieved by simply starting up Mongo on its default port (27017) - for example by running a MongoDB docker image.

### Supported APIs

Upon starting the application, a list of supported APIs can be obtained dynamically from the Swagger Specification
Endpoint:

```http://<host>:<port>/api-docs```

> Substitute `<host>` and `<port` as necessary
