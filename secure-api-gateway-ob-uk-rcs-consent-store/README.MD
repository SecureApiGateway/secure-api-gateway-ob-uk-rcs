# Consent Store Module
This module is responsible for storing OBIE consents in a Mongo database, and exposing a REST API to allow other components
to interact with the store.

## Sub-modules
### datamodel
This contains the public API data-model classes (DTOs) which are sent / received via the REST API

### repo
This module is responsible for MongoDB CRUD operations on Consents.

It contains Consent Entity definitions which are the internal representation of a Consent and services which interact
with the underlying data repositories.

### api
This module is the public REST API for the Consent Store.

It is responsible for implementing the REST Controllers for interacting with the store. These controllers then call
services in the repo module to do the desired operations. The controllers are responsible for converting between the
data-model classes (DTOs) and the store entity representations.

Error responses from this API will be returned as OBErrorResponse1 definitions, reusing the established OBIE error schema.

### client
This module contains a REST API client. Java applications that wish to interact with the Consent Store can use the client
library to build the REST calls more easily, it also provides a mechanism for handling the HTTP error responses and throwing
them on as exceptions.
