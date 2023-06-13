/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.sapi.gateway.rcs.conent.store.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.rcs.conent.store.client.ConsentStoreClientException.ErrorType;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;

public abstract class BaseRestConsentStoreClient {

    private static final String API_CLIENT_ID_HEADER = "x-api-client-id";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final RestTemplate restTemplate;

    protected final ObjectMapper objectMapper;


    public BaseRestConsentStoreClient(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    protected HttpHeaders createHeaders(String apiClientId) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(API_CLIENT_ID_HEADER, apiClientId);
        return headers;
    }

    protected <T> T doRestCall(String url, HttpMethod method, HttpEntity<?> entity, Class<T> responseType) throws ConsentStoreClientException {
        try {
            // TODO apply Java Bean validation to the response
            final ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            return response.getBody();
        } catch (RestClientResponseException ex) {
            throw handleRestClientResponseException(url, method, ex);
        }
    }

    private ConsentStoreClientException handleRestClientResponseException(String url, HttpMethod method, RestClientResponseException ex) {
        final HttpStatus httpStatus = HttpStatus.valueOf(ex.getRawStatusCode());
        logger.info("API call failed - [url: {}}, method: {}] returned - [status: {}]", url, method, httpStatus);
        final OBErrorResponse1 obErrorResponse1 = decodeObErrorResponseObject(ex);

        final OBError1 obError1 = obErrorResponse1.getErrors().get(0);
        if (obErrorResponse1.getCode().equals("OBRI.Consent.Store.Error")) {
            return handleConsentStoreErrorCode(obError1);
        } else {
            return handleGeneralErrorCodes(obError1);
        }
    }

    private ConsentStoreClientException handleConsentStoreErrorCode(OBError1 obError1) {
        ErrorType errorType;
        try {
            errorType = ErrorType.valueOf(obError1.getErrorCode());
        } catch (IllegalArgumentException iae) {
            logger.warn("Unsupported consent store errorCode: {}, returning UNKNOWN error", obError1.getErrorCode());
            errorType = ErrorType.UNKNOWN;
        }
        String message = obError1.getMessage();
        return new ConsentStoreClientException(errorType, message);
    }

    private ConsentStoreClientException handleGeneralErrorCodes(OBError1 obError1) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(obError1.getErrorCode()).append(": ").append(obError1.getMessage());
        if (obError1.getPath() != null) {
            errorMessage.append(" path: ").append(obError1.getPath());
        }
        final ConsentStoreClientException consentStoreClientException = new ConsentStoreClientException(ErrorType.BAD_REQUEST, errorMessage.toString());
        consentStoreClientException.setObError1(obError1);
        return consentStoreClientException;
    }

    private OBErrorResponse1 decodeObErrorResponseObject(RestClientResponseException ex) {
        final OBErrorResponse1 obErrorResponse1;
        try {
            obErrorResponse1 = objectMapper.readValue(ex.getResponseBodyAsByteArray(), OBErrorResponse1.class);
            if (obErrorResponse1.getErrors() == null || obErrorResponse1.getErrors().isEmpty()) {
                logger.error("Badly formed OBErrorResponse1, does not contain any errors");
                throw new ConsentStoreClientException(ErrorType.FAILED_TO_DECODE_RESPONSE, "Failed to decode API error response");
            }
            return obErrorResponse1;
        } catch (IOException e) {
            logger.error("Failed to decode API error response", e);
            throw new ConsentStoreClientException(ErrorType.FAILED_TO_DECODE_RESPONSE, "Failed to decode API error response", e);
        }
    }
}
