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
package com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domestic.v4_0_0;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.rcs.consent.store.client.BaseRestConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v4_0_0.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v4_0_0.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class RestDomesticPaymentConsentStoreClient extends BaseRestConsentStoreClient implements DomesticPaymentConsentStoreClient {
    private final String consentServiceBaseUrl;

    @Autowired
    public RestDomesticPaymentConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration, RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this(consentStoreClientConfiguration, restTemplateBuilder, objectMapper, OBVersion.v3_1_10);
    }

    public RestDomesticPaymentConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration, RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper, OBVersion obVersion) {
        super(restTemplateBuilder, objectMapper);
        String var10001 = consentStoreClientConfiguration.getBaseUri();
        this.consentServiceBaseUrl = var10001 + "/v" + obVersion.getCanonicalVersion() + "/domestic-payment-consents";
    }

    public DomesticPaymentConsent createConsent(CreateDomesticPaymentConsentRequest createConsentRequest) throws ConsentStoreClientException {
        HttpEntity<CreateDomesticPaymentConsentRequest> requestEntity = new HttpEntity(createConsentRequest, this.createHeaders(createConsentRequest.getApiClientId()));
        return (DomesticPaymentConsent)this.doRestCall(this.consentServiceBaseUrl, HttpMethod.POST, requestEntity, DomesticPaymentConsent.class);
    }

    public DomesticPaymentConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException {
        String url = this.consentServiceBaseUrl + "/" + consentId;
        HttpEntity<Object> requestEntity = new HttpEntity(this.createHeaders(apiClientId));
        return (DomesticPaymentConsent)this.doRestCall(url, HttpMethod.GET, requestEntity, DomesticPaymentConsent.class);
    }

    public DomesticPaymentConsent authoriseConsent(AuthorisePaymentConsentRequest authRequest) throws ConsentStoreClientException {
        String var10000 = this.consentServiceBaseUrl;
        String url = var10000 + "/" + authRequest.getConsentId() + "/authorise";
        HttpEntity<AuthorisePaymentConsentRequest> requestEntity = new HttpEntity(authRequest, this.createHeaders(authRequest.getApiClientId()));
        return (DomesticPaymentConsent)this.doRestCall(url, HttpMethod.POST, requestEntity, DomesticPaymentConsent.class);
    }

    public DomesticPaymentConsent rejectConsent(RejectConsentRequest rejectRequest) throws ConsentStoreClientException {
        String var10000 = this.consentServiceBaseUrl;
        String url = var10000 + "/" + rejectRequest.getConsentId() + "/reject";
        HttpEntity<RejectConsentRequest> requestEntity = new HttpEntity(rejectRequest, this.createHeaders(rejectRequest.getApiClientId()));
        return (DomesticPaymentConsent)this.doRestCall(url, HttpMethod.POST, requestEntity, DomesticPaymentConsent.class);
    }

    public DomesticPaymentConsent consumeConsent(ConsumePaymentConsentRequest consumeRequest) throws ConsentStoreClientException {
        String var10000 = this.consentServiceBaseUrl;
        String url = var10000 + "/" + consumeRequest.getConsentId() + "/consume";
        HttpEntity<ConsumePaymentConsentRequest> requestEntity = new HttpEntity(consumeRequest, this.createHeaders(consumeRequest.getApiClientId()));
        return (DomesticPaymentConsent)this.doRestCall(url, HttpMethod.POST, requestEntity, DomesticPaymentConsent.class);
    }
}
