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
package com.forgerock.sapi.gateway.rcs.conent.store.client.account.v3_1_10;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.rcs.conent.store.client.BaseRestConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.conent.store.client.ConsentStoreClientConfiguration;
import com.forgerock.sapi.gateway.rcs.conent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AuthoriseAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.RejectAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

@Component
public class RestAccountAccessConsentStoreClient extends BaseRestConsentStoreClient implements AccountAccessConsentStoreClient {

    private final String consentServiceBaseUrl;

    @Autowired
    public RestAccountAccessConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration,
                                               RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {

        this(consentStoreClientConfiguration, restTemplateBuilder, objectMapper, OBVersion.v3_1_10);
    }

    public RestAccountAccessConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration,
                                               RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper,
                                               OBVersion obVersion) {
        super(restTemplateBuilder, objectMapper);
        this.consentServiceBaseUrl = consentStoreClientConfiguration.getBaseUri() + "/v" + obVersion.getCanonicalVersion() + "/account-access-consents";
    }

    @Override
    public AccountAccessConsent createConsent(CreateAccountAccessConsentRequest createConsentRequest) throws ConsentStoreClientException {
        final HttpEntity<CreateAccountAccessConsentRequest> requestEntity = new HttpEntity<>(createConsentRequest, createHeaders(createConsentRequest.getApiClientId()));
        return doRestCall(consentServiceBaseUrl, HttpMethod.POST, requestEntity, AccountAccessConsent.class);
    }

    @Override
    public AccountAccessConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + consentId;
        final HttpEntity<Object> requestEntity = new HttpEntity<>(createHeaders(apiClientId));
        return doRestCall(url, HttpMethod.GET, requestEntity, AccountAccessConsent.class);
    }

    @Override
    public AccountAccessConsent authoriseConsent(AuthoriseAccountAccessConsentRequest authRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + authRequest.getConsentId() + "/authorise";
        final HttpEntity<AuthoriseAccountAccessConsentRequest> requestEntity = new HttpEntity<>(authRequest, createHeaders(authRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, AccountAccessConsent.class);
    }

    @Override
    public AccountAccessConsent rejectConsent(RejectAccountAccessConsentRequest rejectRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + rejectRequest.getConsentId() + "/reject";
        final HttpEntity<RejectAccountAccessConsentRequest> requestEntity = new HttpEntity<>(rejectRequest, createHeaders(rejectRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, AccountAccessConsent.class);
    }
}
