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
package com.forgerock.sapi.gateway.rcs.conent.store.client.payment.file.v3_1_10;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.rcs.conent.store.client.BaseRestConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.conent.store.client.ConsentStoreClientConfiguration;
import com.forgerock.sapi.gateway.rcs.conent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.file.v3_1_10.CreateFilePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.file.v3_1_10.FilePaymentConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.file.v3_1_10.FileUploadRequest;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

/**
 * Implementation of the FilePaymentConsentStoreClient which makes REST calls over HTTP
 */
@Component
public class RestFilePaymentConsentStoreClient extends BaseRestConsentStoreClient implements FilePaymentConsentStoreClient {

    private final String consentServiceBaseUrl;

    @Autowired
    public RestFilePaymentConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration, RestTemplateBuilder restTemplateBuilder,
                                                 ObjectMapper objectMapper) {
        this(consentStoreClientConfiguration, restTemplateBuilder, objectMapper, OBVersion.v3_1_10);
    }

    public RestFilePaymentConsentStoreClient(ConsentStoreClientConfiguration consentStoreClientConfiguration, RestTemplateBuilder restTemplateBuilder,
                                                 ObjectMapper objectMapper, OBVersion obVersion) {
        super(restTemplateBuilder, objectMapper);
        this.consentServiceBaseUrl = consentStoreClientConfiguration.getBaseUri() + "/v" + obVersion.getCanonicalVersion() + "/file-payment-consents";
    }

    @Override
    public FilePaymentConsent createConsent(CreateFilePaymentConsentRequest createConsentRequest) throws ConsentStoreClientException {
        final HttpEntity<CreateFilePaymentConsentRequest> requestEntity = new HttpEntity<>(createConsentRequest, createHeaders(createConsentRequest.getApiClientId()));
        return doRestCall(consentServiceBaseUrl, HttpMethod.POST, requestEntity, FilePaymentConsent.class);
    }

    @Override
    public FilePaymentConsent uploadFile(FileUploadRequest fileUploadRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + fileUploadRequest.getConsentId() + "/file";
        final HttpEntity<FileUploadRequest> requestEntity = new HttpEntity<>(fileUploadRequest, createHeaders(fileUploadRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, FilePaymentConsent.class);
    }

    @Override
    public FilePaymentConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + consentId;
        final HttpEntity<Object> requestEntity = new HttpEntity<>(createHeaders(apiClientId));
        return doRestCall(url, HttpMethod.GET, requestEntity, FilePaymentConsent.class);
    }

    @Override
    public FilePaymentConsent authoriseConsent(AuthorisePaymentConsentRequest authRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + authRequest.getConsentId() + "/authorise";
        final HttpEntity<AuthorisePaymentConsentRequest> requestEntity = new HttpEntity<>(authRequest, createHeaders(authRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, FilePaymentConsent.class);
    }

    @Override
    public FilePaymentConsent rejectConsent(RejectConsentRequest rejectRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + rejectRequest.getConsentId() + "/reject";
        final HttpEntity<RejectConsentRequest> requestEntity = new HttpEntity<>(rejectRequest, createHeaders(rejectRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, FilePaymentConsent.class);
    }

    @Override
    public FilePaymentConsent consumeConsent(ConsumePaymentConsentRequest consumeRequest) throws ConsentStoreClientException {
        final String url = consentServiceBaseUrl + "/" + consumeRequest.getConsentId() + "/consume";
        final HttpEntity<ConsumePaymentConsentRequest> requestEntity = new HttpEntity<>(consumeRequest, createHeaders(consumeRequest.getApiClientId()));
        return doRestCall(url, HttpMethod.POST, requestEntity, FilePaymentConsent.class);
    }

}
