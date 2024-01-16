/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rcs.consent.store.client.payment.international.v3_1_10;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.rcs.consent.store.client.BaseRestConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.CreateInternationalPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.InternationalPaymentConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

/**
 * Implementation of the InternationalPaymentConsentStoreClient which makes REST calls over HTTP
 */
@Component
public class RestInternationalPaymentConsentStoreClient extends BaseRestConsentStoreClient implements InternationalPaymentConsentStoreClient {

    private final String consentServiceBaseUrl;

    @Autowired
    public RestInternationalPaymentConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration, RestTemplateBuilder restTemplateBuilder,
                                                 ObjectMapper objectMapper) {
        this(consentStoreClientConfiguration, restTemplateBuilder, objectMapper, OBVersion.v3_1_10);
    }

    public RestInternationalPaymentConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration, RestTemplateBuilder restTemplateBuilder,
                                                 ObjectMapper objectMapper, OBVersion obVersion) {
        super(restTemplateBuilder, objectMapper);
        this.consentServiceBaseUrl = consentStoreClientConfiguration.getBaseUri() + "/v" + obVersion.getCanonicalVersion() + "/international-payment-consents";
    }

    @Override
    public InternationalPaymentConsent createConsent(CreateInternationalPaymentConsentRequest createConsentRequest) throws ConsentStoreClientException {
        final HttpEntity<CreateInternationalPaymentConsentRequest> requestEntity = new HttpEntity<>(createConsentRequest, createHeaders(createConsentRequest.getApiClientId()));
        return doRestCall(consentServiceBaseUrl, HttpMethod.POST, requestEntity, InternationalPaymentConsent.class);
    }

    @Override
    public InternationalPaymentConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + consentId;
        final HttpEntity<Object> requestEntity = new HttpEntity<>(createHeaders(apiClientId));
        return doRestCall(url, HttpMethod.GET, requestEntity, InternationalPaymentConsent.class);
    }

    @Override
    public InternationalPaymentConsent authoriseConsent(AuthorisePaymentConsentRequest authRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + authRequest.getConsentId() + "/authorise";
        final HttpEntity<AuthorisePaymentConsentRequest> requestEntity = new HttpEntity<>(authRequest, createHeaders(authRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, InternationalPaymentConsent.class);
    }

    @Override
    public InternationalPaymentConsent rejectConsent(RejectConsentRequest rejectRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + rejectRequest.getConsentId() + "/reject";
        final HttpEntity<RejectConsentRequest> requestEntity = new HttpEntity<>(rejectRequest, createHeaders(rejectRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, InternationalPaymentConsent.class);
    }

    @Override
    public InternationalPaymentConsent consumeConsent(ConsumePaymentConsentRequest consumeRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + consumeRequest.getConsentId() + "/consume";
        final HttpEntity<ConsumePaymentConsentRequest> requestEntity = new HttpEntity<>(consumeRequest, createHeaders(consumeRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, InternationalPaymentConsent.class);
    }

}
