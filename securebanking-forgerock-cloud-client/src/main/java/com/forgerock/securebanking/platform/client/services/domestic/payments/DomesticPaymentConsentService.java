/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.securebanking.platform.client.services.domestic.payments;

import com.forgerock.securebanking.platform.client.configuration.ConfigurationPropertiesClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentDetails;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentRequest;
import com.forgerock.securebanking.platform.client.models.general.Consent;
import com.forgerock.securebanking.platform.client.models.general.ConsentDecision;
import com.forgerock.securebanking.platform.client.utils.url.UrlContext;
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
public class DomesticPaymentConsentService implements DomesticPaymentConsentServiceInterface {

    private final RestTemplate restTemplate;
    private final ConfigurationPropertiesClient configurationProperties;

    public DomesticPaymentConsentService(RestTemplate restTemplate, ConfigurationPropertiesClient configurationProperties) {
        this.restTemplate = restTemplate;
        this.configurationProperties = configurationProperties;
    }

    @Override
    public DomesticPaymentConsentDetails getConsent(DomesticPaymentConsentRequest consentRequest) throws ExceptionClient {
        log.debug("Received an domestic payment access consent request with JWT: '{}'", consentRequest.getConsentRequestJwtString());
        String domesticPaymentIntendId = consentRequest.getIntentId();
        log.debug("=> The domestic payment consent detailsRequest id: '{}'", domesticPaymentIntendId);
        String clientId = consentRequest.getClientId();
        log.debug("=> The client id: '{}'", clientId);

        DomesticPaymentConsentDetails consentDetails = request(consentRequest.getIntentId(), GET, null);
        String errorMessage;
        if (consentDetails == null) {
            errorMessage = String.format("The PISP '%s' is referencing an domestic payment consent detailsRequest '%s' that doesn't exist", clientId, domesticPaymentIntendId);
            log.error(errorMessage);
            throw new ExceptionClient(consentRequest, ErrorType.NOT_FOUND, errorMessage);
        }

        // Verify the PISP is the same than the one that created this consent ^
        if (!clientId.equals(consentDetails.getOauth2ClientId())) {
            errorMessage = String.format("The PISP '%S' created the domestic payment consent detailsRequest '%S' but it's PISP '%s' that is trying to get" +
                    " consent for it.", consentDetails.getOauth2ClientId(), domesticPaymentIntendId, clientId);
            log.error(errorMessage);
            throw new ExceptionClient(consentRequest, ErrorType.INVALID_REQUEST, errorMessage);
        }

        return consentDetails;
    }

    @Override
    public Consent updateConsent(ConsentDecision consentDecision) throws ExceptionClient {
        String domesticPaymentIntendId = consentDecision.getIntentId();
        log.debug("Received an request to update the consent: '{}'", domesticPaymentIntendId);
        log.debug("=> The owner id: '{}'", consentDecision.getResourceOwnerUsername());
        HttpEntity<ConsentDecision> requestEntity = new HttpEntity<>(consentDecision, getHeaders());
        DomesticPaymentConsentDetails consentDetails = request(domesticPaymentIntendId, PATCH, requestEntity);
        return consentDetails;
    }

    private DomesticPaymentConsentDetails request(String intentId, HttpMethod httpMethod, HttpEntity httpEntity) throws ExceptionClient {
        String consentURL = configurationProperties.getIgServer() +
                UrlContext.replaceParameterContextIntentId(
                        configurationProperties.getContextsDomesticPaymentConsent().get(GET.name()),
                        intentId
                );
        log.debug("(DomesticPaymentConsentService#request) request the consent details from platform: {}", consentURL);
        try {
            ResponseEntity<DomesticPaymentConsentDetails> responseEntity = restTemplate.exchange(
                    consentURL,
                    httpMethod,
                    httpEntity,
                    DomesticPaymentConsentDetails.class);

            return responseEntity != null ? responseEntity.getBody() : null;
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
