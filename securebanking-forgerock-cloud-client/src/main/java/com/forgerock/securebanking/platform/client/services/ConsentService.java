/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.platform.client.services;

import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.configuration.ConfigurationPropertiesClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.base.ConsentDecision;
import com.forgerock.securebanking.platform.client.models.base.ConsentRequest;
import com.forgerock.securebanking.platform.client.utils.url.UrlContext;
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
@ComponentScan(basePackages = {"com.forgerock.securebanking.platform.client.configuration"})
public class ConsentService implements ConsentServiceInterface {

    private final RestTemplate restTemplate;
    private final ConfigurationPropertiesClient configurationProperties;

    public ConsentService(RestTemplate restTemplate, ConfigurationPropertiesClient configurationProperties) {
        this.restTemplate = restTemplate;
        this.configurationProperties = configurationProperties;
    }

    @Override
    public JsonObject getConsent(ConsentRequest consentRequest) throws ExceptionClient {
        log.debug("Received a consent request with JWT: '{}'", consentRequest.getConsentRequestJwtString());
        String intentId = consentRequest.getIntentId();
        log.debug("=> The consent detailsRequest id: '{}'", intentId);
        String clientId = consentRequest.getClientId();
        log.debug("=> The client id: '{}'", clientId);

        JsonObject consentDetails = request(consentRequest.getIntentId(), GET, null);
        String errorMessage;
        if (consentDetails == null) {
            errorMessage = String.format("The PISP/AISP '%s' is referencing a consent detailsRequest '%s' that doesn't exist", clientId, intentId);
            log.error(errorMessage);
            throw new ExceptionClient(consentRequest, ErrorType.NOT_FOUND, errorMessage);
        }

        // Verify the PISP/AISP is the same than the one that created this consent ^
        if (!clientId.equals(consentDetails.get("oauth2ClientId").getAsString())) {
            errorMessage = String.format("The PISP/AISP '%S' created the consent detailsRequest '%S' but it's PISP/AISP '%s' that is trying to get" +
                    " consent for it.", consentDetails.get("oauth2ClientId"), intentId, clientId);
            log.error(errorMessage);
            throw new ExceptionClient(consentRequest, ErrorType.INVALID_REQUEST, errorMessage);
        }

        return consentDetails;
    }

    @Override
    public JsonObject updateConsent(ConsentDecision consentDecision) throws ExceptionClient {
        String domesticPaymentIntendId = consentDecision.getIntentId();
        log.debug("Received an request to update the consent: '{}'", domesticPaymentIntendId);
        log.debug("=> The owner id: '{}'", consentDecision.getResourceOwnerUsername());
        HttpEntity<ConsentDecision> requestEntity = new HttpEntity<>(consentDecision, getHeaders());
        JsonObject consentDetails = request(domesticPaymentIntendId, PATCH, requestEntity);
        return consentDetails;
    }

    private JsonObject request(String intentId, HttpMethod httpMethod, HttpEntity httpEntity) throws ExceptionClient {
        String consentURL;
        switch (IntentType.identify(intentId)) {
            case ACCOUNT_ACCESS_CONSENT -> {
                consentURL = configurationProperties.getIgFqdn() +
                        UrlContext.replaceParameterContextIntentId(
                                configurationProperties.getContextsAccountsConsent().get(GET.name()),
                                intentId
                        );
            }
            case PAYMENT_DOMESTIC_CONSENT -> {
                consentURL = configurationProperties.getIgFqdn() +
                        UrlContext.replaceParameterContextIntentId(
                                configurationProperties.getContextsDomesticPaymentConsent().get(GET.name()),
                                intentId
                        );
            }
            default -> {
                String message = String.format("Invalid type for intent ID: '%s'", intentId);
                throw new ExceptionClient(new ConsentDecision(), ErrorType.UNKNOWN_INTENT_TYPE, message);
            }
        }


        log.debug("(ConsentService#request) request the consent details from platform: {}", consentURL);
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
