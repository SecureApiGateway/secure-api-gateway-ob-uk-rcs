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
package com.forgerock.sapi.gateway.rcs.consent.store.client.customerinfo.v1_0;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.rcs.consent.store.client.BaseRestConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.AuthoriseCustomerInfoConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.CreateCustomerInfoConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.CustomerInfoConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RestCustomerInfoConsentStoreClient extends BaseRestConsentStoreClient implements CustomerInfoConsentStoreClient {

    private final String consentServiceBaseUrl;

    @Autowired
    public RestCustomerInfoConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration,
                                              RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {

        this(consentStoreClientConfiguration, restTemplateBuilder, objectMapper, OBVersion.v1_0);
    }

    public RestCustomerInfoConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration,
                                              RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper,
                                              OBVersion obVersion) {
        super(restTemplateBuilder, objectMapper);
        Objects.requireNonNull(consentStoreClientConfiguration, "consentStoreClientConfiguration must be provided");
        this.consentServiceBaseUrl = consentStoreClientConfiguration.getBaseUri() + "/" + obVersion.getCanonicalName() + "/customer-info-consents";
    }


    @Override
    public CustomerInfoConsent createConsent(CreateCustomerInfoConsentRequest createConsentRequest) throws ConsentStoreClientException {
        final HttpEntity<CreateCustomerInfoConsentRequest> requestEntity = new HttpEntity<>(createConsentRequest, createHeaders(createConsentRequest.getApiClientId()));
        return doRestCall(consentServiceBaseUrl, HttpMethod.POST, requestEntity, CustomerInfoConsent.class);
    }

    @Override
    public CustomerInfoConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + consentId;
        final HttpEntity<Object> requestEntity = new HttpEntity<>(createHeaders(apiClientId));
        return doRestCall(url, HttpMethod.GET, requestEntity, CustomerInfoConsent.class);
    }

    @Override
    public CustomerInfoConsent authoriseConsent(AuthoriseCustomerInfoConsentRequest authRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + authRequest.getConsentId() + "/authorise";
        final HttpEntity<AuthoriseCustomerInfoConsentRequest> requestEntity = new HttpEntity<>(authRequest, createHeaders(authRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, CustomerInfoConsent.class);
    }

    @Override
    public CustomerInfoConsent rejectConsent(RejectConsentRequest rejectRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + rejectRequest.getConsentId() + "/reject";
        final HttpEntity<RejectConsentRequest> requestEntity = new HttpEntity<>(rejectRequest, createHeaders(rejectRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, CustomerInfoConsent.class);
    }

    @Override
    public void deleteConsent(String consentId, String apiClientId) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + consentId;
        final HttpEntity<Object> requestEntity = new HttpEntity<>(createHeaders(apiClientId));
        doRestCall(url, HttpMethod.DELETE, requestEntity, Void.class);
    }
}
