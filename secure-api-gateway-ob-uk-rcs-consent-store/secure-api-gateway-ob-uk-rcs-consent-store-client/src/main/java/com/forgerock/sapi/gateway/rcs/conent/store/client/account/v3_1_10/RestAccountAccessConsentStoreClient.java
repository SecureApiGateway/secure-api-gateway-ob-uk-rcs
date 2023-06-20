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
