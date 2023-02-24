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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services;

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.IntentType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.configuration.ConsentRepoConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDecisionRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.utils.url.UrlContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Specific implementation service to retrieve the Domestic Payment Consent Details from the platform
 */
@Service
@Slf4j
@ComponentScan(basePackages = {"com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.configuration"})
public class ConsentService implements ConsentServiceInterface {

    private final RestTemplate restTemplate;
    private final ConsentRepoConfiguration consentRepoConfiguration;

    public ConsentService(RestTemplate restTemplate, ConsentRepoConfiguration consentRepoConfiguration) {
        this.restTemplate = restTemplate;
        this.consentRepoConfiguration = consentRepoConfiguration;
    }

    @Override
    public JsonObject getConsent(ConsentClientDetailsRequest consentRequest) throws ExceptionClient {
        log.debug("Received a consent request with JWT: '{}'", consentRequest.getConsentRequestJwtString());
        String intentId = consentRequest.getIntentId();
        log.debug("=> The consent detailsRequest id: '{}'", intentId);
        String clientId = consentRequest.getClientId();
        log.debug("=> The client id: '{}'", clientId);

        JsonObject consentDetails = request(consentRequest.getIntentId(), GET, null);
        if (consentDetails == null) {
            log.error("TPP: {} is trying to access consent: {} that does not exist", clientId, intentId);
            throw new ExceptionClient(consentRequest, ErrorType.NOT_FOUND, "The consent: " + intentId + " does not exist");
        }

        // Verify the PISP/AISP is the same as the one that created this consent
        if (!clientId.equals(consentDetails.get("oauth2ClientId").getAsString())) {
            log.error("TPP: {} is trying to access consent: {} that it is not authorised to access", clientId, intentId);
            throw new ExceptionClient(consentRequest, ErrorType.INVALID_REQUEST, "You are not authorised to access consent: " + intentId);
        }

        return consentDetails;
    }

    @Override
    public JsonObject updateConsent(ConsentClientDecisionRequest consentDecision) throws ExceptionClient {
        String intendId = consentDecision.getIntentId();
        log.debug("Received an request to update the consent: '{}'", consentDecision);
        log.debug("=> The owner id: '{}'", consentDecision.getResourceOwnerUsername());
        HttpEntity<ConsentClientDecisionRequest> requestEntity = new HttpEntity<>(consentDecision, getHeaders());
        JsonObject consentDetails = request(intendId, PATCH, requestEntity);
        return consentDetails;
    }

    private JsonObject request(String intentId, HttpMethod httpMethod, HttpEntity httpEntity) throws ExceptionClient {
        String consentURL;
        IntentType intentType = IntentType.identify(intentId);
        if (intentType == null) {
            String errorMessage = String.format("It has not been possible identify the intent type '%s'.", intentId);
            log.error("(ConsentService#request) {}", errorMessage);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.UNKNOWN_INTENT_TYPE)
                            .intentId(intentId)
                            .build(),
                    errorMessage
            );
        }
        consentURL = consentRepoConfiguration.getConsentRepoBaseUri() +
                UrlContext.replaceParameterContextIntentId(
                        consentRepoConfiguration.getContextsRepoConsent().get(httpMethod.name()),
                        intentId
                );

        log.debug("(ConsentService#request) {} the consent details from platform: {}", httpMethod.name(), consentURL);
        log.debug("Entity To {}: {}", httpMethod.name(), httpEntity != null ? httpEntity.getBody().toString() : "null");
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    consentURL,
                    httpMethod,
                    httpEntity,
                    String.class);
            log.debug("(ConsentService#request) response entity: " + responseEntity);

            return responseEntity != null && responseEntity.getBody() != null ? new JsonParser().parse(responseEntity.getBody()).getAsJsonObject() : null;
        } catch (RestClientException e) {
            log.error(ErrorType.SERVER_ERROR.getDescription(), e);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.SERVER_ERROR)
                            .intentId(intentId)
                            .build(),
                    e.getMessage()
            );
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        // TODO - add additional required headers
        return headers;
    }
}
