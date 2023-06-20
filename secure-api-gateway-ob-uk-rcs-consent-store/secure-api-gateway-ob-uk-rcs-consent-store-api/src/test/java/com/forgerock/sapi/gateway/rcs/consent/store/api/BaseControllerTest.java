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
package com.forgerock.sapi.gateway.rcs.consent.store.api;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.ApiTestUtils.createConsentStoreApiRequiredHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.BaseAuthoriseConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.BaseConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.BaseCreateConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.RejectConsentRequest;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public abstract class BaseControllerTest<T extends BaseConsent, C extends BaseCreateConsentRequest, A extends BaseAuthoriseConsentRequest> {

    protected static final String TEST_API_CLIENT_1 ="test-api-client-1";

    protected static final String TEST_RESOURCE_OWNER_ID = "psu4test";

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    protected String apiBaseUrl;

    protected final Class<T> consentClass;

    protected BaseControllerTest(Class<T> consentClass) {
        this.consentClass = consentClass;
    }

    @PostConstruct
    public void postConstruct() {
        apiBaseUrl = "http://localhost:" + port + "/consent/store/v3.1.10/" + getControllerEndpointName();
    }

    protected abstract String getControllerEndpointName();

    protected abstract C buildCreateConsentRequest(String apiClientId);

    protected abstract void validateCreateConsentAgainstCreateRequest(T consent, C createConsentRequest);

    protected abstract A buildAuthoriseConsentRequest(T consent, String resourceOwnerId);

    protected abstract void validateAuthorisedConsent(T authorisedConsent, A authoriseConsentReq, T originalConsent);

    protected abstract void validateRejectedConsent(T rejectedConsent, RejectConsentRequest rejectConsentRequest, T originalConsent);

    protected T createConsent(String apiClient) {
        return createConsent(buildCreateConsentRequest(apiClient));
    }

    protected T createConsent(C createConsentRequest) {
        final ResponseEntity<T> consentResponseEntity = makePostRequest(createConsentRequest, consentClass);
        assertThat(consentResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final T consent = consentResponseEntity.getBody();
        validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);

        return consent;
    }

    protected static void validateInvalidPermissionsErrorResponse(String consentId, ResponseEntity<OBErrorResponse1> authoriseConsentResponse) {
        assertThat(authoriseConsentResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        final OBErrorResponse1 errorResponse = authoriseConsentResponse.getBody();
        assertThat(errorResponse.getCode()).isEqualTo("OBRI.Consent.Store.Error");
        assertThat(errorResponse.getId()).isNotNull(); // TODO test with x-fapi-interaction-id
        assertThat(errorResponse.getMessage()).isEqualTo(HttpStatus.FORBIDDEN.name());
        assertThat(errorResponse.getErrors()).hasSize(1);
        final OBError1 obError = errorResponse.getErrors().get(0);
        assertThat(obError.getErrorCode()).isEqualTo("INVALID_PERMISSIONS");
        assertThat(obError.getMessage()).isEqualTo("INVALID_PERMISSIONS for consentId: " + consentId);
    }

    @Test
    public void failToGetConsentDoesNotExist() {
        final ResponseEntity<OBErrorResponse1> getResponse = makeGetRequest("unknown-consent", "client-123", OBErrorResponse1.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void failToCreateConsentRequestMissingRequiredFields() {
        final C invalidRequest = buildCreateConsentRequest(null);
        final ResponseEntity<OBErrorResponse1> createConsentResponse = makePostRequest(invalidRequest, OBErrorResponse1.class);
        final OBErrorResponse1 errorResponse = createConsentResponse.getBody();
        assertThat(createConsentResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getCode()).isEqualTo("OBRI.Argument.Invalid");
        assertThat(errorResponse.getErrors()).isNotEmpty().
                contains(new OBError1().errorCode("UK.OBIE.Field.Invalid")
                        .message("The field received is invalid. Reason 'must not be null'")
                        .path("apiClientId"));
    }

    @Test
    public void testSuccessfullyCreatingConsent() {
        final T consent = createConsent(buildCreateConsentRequest(TEST_API_CLIENT_1));

        final ResponseEntity<T> getConsentResponseEntity = makeGetRequest(consent.getId(), consent.getApiClientId(), consentClass);
        assertThat(getConsentResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        final T getConsentResponse = getConsentResponseEntity.getBody();
        assertThat(getConsentResponse).usingRecursiveComparison().isEqualTo(consent);
    }

    @Test
    public void failToGetConsentForDifferentApiClient() {
        final T client1Consent = createConsent(TEST_API_CLIENT_1);
        final ResponseEntity<OBErrorResponse1> getConsentResponseEntity = makeGetRequest(client1Consent.getId(),
                "client-2", OBErrorResponse1.class);
        validateInvalidPermissionsErrorResponse(client1Consent.getId(), getConsentResponseEntity);
    }

    @Test
    public void authoriseConsent() {
        final T consent = createConsent(TEST_API_CLIENT_1);
        final A authoriseReq = buildAuthoriseConsentRequest(consent, TEST_RESOURCE_OWNER_ID);

        final ResponseEntity<T> authoriseConsentResponse = authoriseConsent(authoriseReq, consentClass);
        assertThat(authoriseConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        final T authorisedConsent = authoriseConsentResponse.getBody();
        validateAuthorisedConsent(authorisedConsent, authoriseReq, consent);
    }

    @Test
    public void failToAuthoriseConsentCreatedByDifferentApiClient() {
        final T consent = createConsent(TEST_API_CLIENT_1);
        final A authoriseReq = buildAuthoriseConsentRequest(consent, TEST_RESOURCE_OWNER_ID);
        authoriseReq.setApiClientId("another-client");

        final ResponseEntity<OBErrorResponse1> authoriseConsentResponse = authoriseConsent(authoriseReq, OBErrorResponse1.class);
        validateInvalidPermissionsErrorResponse(consent.getId(), authoriseConsentResponse);
    }

    @Test
    public void rejectConsent() {
        final T consent = createConsent(TEST_API_CLIENT_1);

        final RejectConsentRequest rejectRequest = new RejectConsentRequest();
        rejectRequest.setConsentId(consent.getId());
        rejectRequest.setResourceOwnerId(TEST_RESOURCE_OWNER_ID);
        rejectRequest.setApiClientId(TEST_API_CLIENT_1);

        final ResponseEntity<T> rejectResponse = rejectConsent(rejectRequest, consentClass);
        assertThat(rejectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        final T rejectedConsent = rejectResponse.getBody();
        validateRejectedConsent(rejectedConsent, rejectRequest, consent);
    }

    @Test
    public void failToRejectConsentCreatedByDifferentApiClient() {
        final T consent = createConsent(TEST_API_CLIENT_1);

        final RejectConsentRequest rejectRequest = new RejectConsentRequest();
        rejectRequest.setConsentId(consent.getId());
        rejectRequest.setResourceOwnerId(TEST_RESOURCE_OWNER_ID);
        rejectRequest.setApiClientId("another-client-id");

        final ResponseEntity<OBErrorResponse1> rejectResponse = rejectConsent(rejectRequest, OBErrorResponse1.class);
        validateInvalidPermissionsErrorResponse(consent.getId(), rejectResponse);
    }


    protected <T> ResponseEntity<T> makeGetRequest(String consentId, String apiClientId, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + consentId,
                HttpMethod.GET,
                new HttpEntity<>(createConsentStoreApiRequiredHeaders(apiClientId)),
                responseClass);
    }

    protected <T> ResponseEntity<T> makePostRequest(C createConsentRequest, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl, HttpMethod.POST,
                new HttpEntity<>(createConsentRequest, createConsentStoreApiRequiredHeaders(createConsentRequest.getApiClientId())),
                responseClass);
    }

    protected <T> ResponseEntity<T> authoriseConsent(A authoriseReq, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + authoriseReq.getConsentId() + "/authorise", HttpMethod.POST,
                new HttpEntity<>(authoriseReq, createConsentStoreApiRequiredHeaders(authoriseReq.getApiClientId())),
                responseClass);
    }

    protected <T> ResponseEntity<T> rejectConsent(RejectConsentRequest rejectRequest, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + rejectRequest.getConsentId() + "/reject", HttpMethod.POST,
                new HttpEntity<>(rejectRequest, createConsentStoreApiRequiredHeaders(rejectRequest.getApiClientId())),
                responseClass);
    }
}
