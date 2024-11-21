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
package com.forgerock.sapi.gateway.rcs.consent.store.client.payment.internationalscheduled;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.rcs.consent.store.client.BaseRestConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.CreateInternationalScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.InternationalScheduledPaymentConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

/**
 * Implementation of the InternationalScheduledPaymentConsentStoreClient which makes REST calls over HTTP
 */
public class BaseRestInternationalScheduledPaymentConsentStoreClient extends BaseRestConsentStoreClient implements InternationalScheduledPaymentConsentStoreClient {

    private final String consentServiceBaseUrl;

    public BaseRestInternationalScheduledPaymentConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration, RestTemplateBuilder restTemplateBuilder,
                                                                   ObjectMapper objectMapper, OBVersion obVersion) {
        super(restTemplateBuilder, objectMapper);
        this.consentServiceBaseUrl = consentStoreClientConfiguration.getBaseUri() + "/v" + obVersion.getCanonicalVersion() + "/international-scheduled-payment-consents";
    }

    @Override
    public InternationalScheduledPaymentConsent createConsent(CreateInternationalScheduledPaymentConsentRequest createConsentRequest) throws ConsentStoreClientException {
        final HttpEntity<CreateInternationalScheduledPaymentConsentRequest> requestEntity = new HttpEntity<>(createConsentRequest, createHeaders(createConsentRequest.getApiClientId()));
        return doRestCall(consentServiceBaseUrl, HttpMethod.POST, requestEntity, InternationalScheduledPaymentConsent.class);
    }

    @Override
    public InternationalScheduledPaymentConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + consentId;
        final HttpEntity<Object> requestEntity = new HttpEntity<>(createHeaders(apiClientId));
        return doRestCall(url, HttpMethod.GET, requestEntity, InternationalScheduledPaymentConsent.class);
    }

    @Override
    public InternationalScheduledPaymentConsent authoriseConsent(AuthorisePaymentConsentRequest authRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + authRequest.getConsentId() + "/authorise";
        final HttpEntity<AuthorisePaymentConsentRequest> requestEntity = new HttpEntity<>(authRequest, createHeaders(authRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, InternationalScheduledPaymentConsent.class);
    }

    @Override
    public InternationalScheduledPaymentConsent rejectConsent(RejectConsentRequest rejectRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + rejectRequest.getConsentId() + "/reject";
        final HttpEntity<RejectConsentRequest> requestEntity = new HttpEntity<>(rejectRequest, createHeaders(rejectRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, InternationalScheduledPaymentConsent.class);
    }

    @Override
    public InternationalScheduledPaymentConsent consumeConsent(ConsumePaymentConsentRequest consumeRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + consumeRequest.getConsentId() + "/consume";
        final HttpEntity<ConsumePaymentConsentRequest> requestEntity = new HttpEntity<>(consumeRequest, createHeaders(consumeRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, InternationalScheduledPaymentConsent.class);
    }

}
