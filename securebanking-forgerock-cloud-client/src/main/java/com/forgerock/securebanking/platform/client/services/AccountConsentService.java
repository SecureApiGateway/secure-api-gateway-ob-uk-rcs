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

import com.forgerock.securebanking.platform.client.configuration.ConfigurationPropertiesClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.AccountConsentDetails;
import com.forgerock.securebanking.platform.client.models.Consent;
import com.forgerock.securebanking.platform.client.models.ConsentDecision;
import com.forgerock.securebanking.platform.client.models.ConsentRequest;
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
 * Specific implementation service to retrieve the Account Consent Details from the platform
 */
@Service
@Slf4j
@ComponentScan(basePackages = {"com.forgerock.securebanking.platform.client.configuration"})
public class AccountConsentService implements ConsentService {

    private final RestTemplate restTemplate;
    private final ConfigurationPropertiesClient configurationProperties;

    public AccountConsentService(RestTemplate restTemplate, ConfigurationPropertiesClient configurationProperties) {
        this.restTemplate = restTemplate;
        this.configurationProperties = configurationProperties;
    }

    @Override
    public AccountConsentDetails getConsent(ConsentRequest consentRequest) throws ExceptionClient {
        log.debug("Received an account access consent request with JWT: '{}'", consentRequest.getConsentRequestJwtString());
        String accountIntendId = consentRequest.getIntentId();
        log.debug("=> The account consent detailsRequest id: '{}'", accountIntendId);
        String clientId = consentRequest.getClientId();
        log.debug("=> The client id: '{}'", clientId);

        AccountConsentDetails accountConsentDetails = request(consentRequest.getIntentId(), GET, null);
        String errorMessage;
        if (accountConsentDetails == null) {
            errorMessage = String.format("The AISP '%s' is referencing an account consent detailsRequest '%s' that doesn't exist", clientId, accountIntendId);
            log.error(errorMessage);
            throw new ExceptionClient(consentRequest, ErrorType.NOT_FOUND, errorMessage);
        }

        // Verify the AISP is the same than the one that created this accountConsent ^
        if (!clientId.equals(accountConsentDetails.getOauth2ClientId())) {
            errorMessage = String.format("The AISP '%S' created the account consent detailsRequest '%S' but it's AISP '%s' that is trying to get" +
                    " consent for it.", accountConsentDetails.getOauth2ClientId(), accountIntendId, clientId);
            log.error(errorMessage);
            throw new ExceptionClient(consentRequest, ErrorType.INVALID_REQUEST, errorMessage);
        }

        return accountConsentDetails;
    }

    @Override
    public Consent updateConsent(ConsentDecision consentDecision) throws ExceptionClient {
        String accountIntendId = consentDecision.getIntentId();
        log.debug("Received an request to update the consent: '{}'", accountIntendId);
        log.debug("=> The owner id: '{}'", consentDecision.getResourceOwnerUsername());
        HttpEntity<ConsentDecision> requestEntity = new HttpEntity<>(consentDecision, getHeaders());
        AccountConsentDetails accountConsentDetails = request(accountIntendId, PATCH, requestEntity);
        return accountConsentDetails;
    }

    private AccountConsentDetails request(String intentId, HttpMethod httpMethod, HttpEntity httpEntity) throws ExceptionClient {
        String consentURL = configurationProperties.getIgFqdn() +
                UrlContext.replaceParameterContextIntentId(
                        configurationProperties.getContextsAccountsConsent().get(GET.name()),
                        intentId
                );
        log.debug("(AccountConsentService#request) request the consent details from platform: {}", consentURL);
        try {
            ResponseEntity<AccountConsentDetails> responseEntity = restTemplate.exchange(
                    consentURL,
                    httpMethod,
                    httpEntity,
                    AccountConsentDetails.class);

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
