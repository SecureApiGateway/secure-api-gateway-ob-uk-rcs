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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.AuthoriseDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.ConsumeDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.RejectDomesticPaymentConsentRequest;

@Component
public class RestDomesticPaymentConsentApiClient implements DomesticPaymentConsentApiClient {

    private final String consentServiceBaseUrl;

    private final RestTemplate restTemplate;

    public RestDomesticPaymentConsentApiClient(ConsentServiceClientConfiguration consentServiceClientConfiguration, RestTemplate restTemplate) {
        // TODO Make version configurable
        this.consentServiceBaseUrl = consentServiceClientConfiguration.getBaseUrl() + "/v3.1.10/domestic-payment-consents";
        this.restTemplate = restTemplate;
    }

    @Override
    public DomesticPaymentConsent createConsent(CreateDomesticPaymentConsentRequest createConsentRequest) {
        final ResponseEntity<DomesticPaymentConsent> consentResponse = restTemplate.exchange(consentServiceBaseUrl, HttpMethod.POST,
                new HttpEntity<>(createConsentRequest, createHeaders(createConsentRequest.getApiClientId())),
                DomesticPaymentConsent.class);

        // TODO error handling
        return consentResponse.getBody();
    }

    @Override
    public DomesticPaymentConsent getConsent(String consentId, String apiClientId) {
        final ResponseEntity<DomesticPaymentConsent> getResponse = restTemplate.exchange(consentServiceBaseUrl + "/" + consentId,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(apiClientId)),
                DomesticPaymentConsent.class);

        // TODO error handling
        return getResponse.getBody();
    }

    @Override
    public DomesticPaymentConsent authoriseConsent(AuthoriseDomesticPaymentConsentRequest authRequest) {
        final ResponseEntity<DomesticPaymentConsent> authResponse = restTemplate.exchange(consentServiceBaseUrl + "/" + authRequest.getConsentId() + "/authorise", HttpMethod.POST,
                new HttpEntity<>(authRequest, createHeaders(authRequest.getApiClientId())),
                DomesticPaymentConsent.class);

        // TODO error handling
        return authResponse.getBody();
    }

    @Override
    public DomesticPaymentConsent rejectConsent(RejectDomesticPaymentConsentRequest rejectRequest) {
        final ResponseEntity<DomesticPaymentConsent> rejectResponse = restTemplate.exchange(consentServiceBaseUrl + "/" + rejectRequest.getConsentId() + "/reject", HttpMethod.POST,
                new HttpEntity<>(rejectRequest, createHeaders(rejectRequest.getApiClientId())),
                DomesticPaymentConsent.class);

        // TODO error handling
        return rejectResponse.getBody();
    }

    @Override
    public DomesticPaymentConsent consumeConsent(ConsumeDomesticPaymentConsentRequest consumeRequest) {
        final ResponseEntity<DomesticPaymentConsent> consumeResponse = restTemplate.exchange(consentServiceBaseUrl + "/" + consumeRequest.getConsentId() + "/consume", HttpMethod.POST,
                new HttpEntity<>(consumeRequest, createHeaders(consumeRequest.getApiClientId())),
                DomesticPaymentConsent.class);

        // TODO error handling
        return consumeResponse.getBody();
    }

    private HttpHeaders createHeaders(String apiClientId) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-client-id", apiClientId);
        return headers;
    }
}
