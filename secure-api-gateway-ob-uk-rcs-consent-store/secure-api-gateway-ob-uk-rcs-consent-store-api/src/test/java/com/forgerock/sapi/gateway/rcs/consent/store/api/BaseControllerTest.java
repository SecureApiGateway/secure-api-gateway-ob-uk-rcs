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
package com.forgerock.sapi.gateway.rcs.consent.store.api;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.ApiTestUtils.createConsentStoreApiRequiredHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.BaseAuthoriseConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.BaseConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.BaseCreateConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import jakarta.annotation.PostConstruct;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;

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
        apiBaseUrl = "http://localhost:" + port + "/consent/store/" + getControllerVersion().getCanonicalName() + "/" +  getControllerEndpointName();
    }

    protected abstract OBVersion getControllerVersion();

    protected abstract String getControllerEndpointName();

    protected abstract C buildCreateConsentRequest(String apiClientId);

    protected abstract void validateCreateConsentAgainstCreateRequest(T consent, C createConsentRequest);

    protected abstract A buildAuthoriseConsentRequest(T consent, String resourceOwnerId);

    protected abstract void validateAuthorisedConsent(T authorisedConsent, A authoriseConsentReq, T originalConsent);

    protected abstract void validateRejectedConsent(T rejectedConsent, RejectConsentRequest rejectConsentRequest, T originalConsent);

    protected T createConsent(String apiClient) {
        return createConsent(buildCreateConsentRequest(apiClient));
    }

    protected T getConsentInStateToAuthoriseOrReject(String apiClientId) {
        return createConsent(apiClientId);
    }

    /**
     * Creates a ConsentEntity directly in the database for use in version validation testing
     *
     * @param apiClient the id of the apiClient that owns the consent
     * @param version OBVersion of the ConsentEntity to create
     * @return the consentId
     */
    protected abstract String createConsentEntityForVersionValidation(String apiClient, OBVersion version);

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
        final String consentId = "unknown-consent";
        final ResponseEntity<OBErrorResponse1> getResponse = makeGetRequest(consentId, "client-123", OBErrorResponse1.class);
        validateConsentNotFoundErrorResponse(consentId, getResponse);
    }

    protected static void validateConsentNotFoundErrorResponse(String consentId, ResponseEntity<OBErrorResponse1> errorResponse) {
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        final OBErrorResponse1 obErrorResponse1 = errorResponse.getBody();
        assertThat(obErrorResponse1.getCode()).isEqualTo("OBRI.Consent.Store.Error");
        assertThat(obErrorResponse1.getId()).isNotNull(); // TODO test with x-fapi-interaction-id
        assertThat(obErrorResponse1.getErrors()).hasSize(1);
        final OBError1 obError = obErrorResponse1.getErrors().get(0);
        assertThat(obError.getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(obError.getMessage()).isEqualTo("NOT_FOUND for consentId: " + consentId);
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
        final T consentToAuthorise = getConsentInStateToAuthoriseOrReject(TEST_API_CLIENT_1);
        final A authoriseReq = buildAuthoriseConsentRequest(consentToAuthorise, TEST_RESOURCE_OWNER_ID);

        final ResponseEntity<T> authoriseConsentResponse = authoriseConsent(authoriseReq, consentClass);
        assertThat(authoriseConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        final T authorisedConsent = authoriseConsentResponse.getBody();
        validateAuthorisedConsent(authorisedConsent, authoriseReq, consentToAuthorise);
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
        final T consentToReject = getConsentInStateToAuthoriseOrReject(TEST_API_CLIENT_1);

        final RejectConsentRequest rejectRequest = new RejectConsentRequest();
        rejectRequest.setConsentId(consentToReject.getId());
        rejectRequest.setResourceOwnerId(TEST_RESOURCE_OWNER_ID);
        rejectRequest.setApiClientId(TEST_API_CLIENT_1);

        final ResponseEntity<T> rejectResponse = rejectConsent(rejectRequest, consentClass);
        assertThat(rejectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        final T rejectedConsent = rejectResponse.getBody();
        validateRejectedConsent(rejectedConsent, rejectRequest, consentToReject);
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

    @Test
    @EnabledIf("controllerVersionIsGreaterThanV3_1_9")
    public void shouldAccessConsentCreatedUsingOlderApiVersion() {
        final String consentId = createConsentEntityForVersionValidation(TEST_API_CLIENT_1, OBVersion.v3_1_9);

        final ResponseEntity<T> getConsentResponseEntity = makeGetRequest(consentId, TEST_API_CLIENT_1, consentClass);

        assertThat(getConsentResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getConsentResponseEntity.getBody().getId()).isEqualTo(consentId);
        assertThat(getConsentResponseEntity.getBody().getApiClientId()).isEqualTo(TEST_API_CLIENT_1);
        assertThat(getConsentResponseEntity.getBody().getRequestVersion()).isEqualTo(OBVersion.v3_1_9);
    }

    boolean controllerVersionIsGreaterThanV3_1_9() {
        return getControllerVersion().isAfterVersion(OBVersion.v3_1_9);
    }

    /**
     * This test is only supported by controllers running version < v4.0.0, this is because it needs to create
     * a consent on a newer API version and v4.0.0 is the latest version that we support (at the time of writing).
     */
    @Test
    @EnabledIf("controllerVersionIsLessThanV4_0_0")
    public void failToAccessConsentCreatedUsingNewerApiVersion() {
        final OBVersion consentVersion = OBVersion.v4_0_0;
        final String consentId = createConsentEntityForVersionValidation(TEST_API_CLIENT_1, consentVersion);

        final ResponseEntity<OBErrorResponse1> errorResponseEntity = makeGetRequest(consentId,
                                                                                    TEST_API_CLIENT_1,
                                                                                    OBErrorResponse1.class);

        validateInvalidApiVersionErrorResponse(consentId, consentVersion, getControllerVersion(), errorResponseEntity);
    }

    boolean controllerVersionIsLessThanV4_0_0() {
        return getControllerVersion().isBeforeVersion(OBVersion.v4_0_0);
    }

    protected static void validateInvalidApiVersionErrorResponse(String consentId, OBVersion consentVersion,
            OBVersion controllerVersion, ResponseEntity<OBErrorResponse1> errorResponse) {
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final OBErrorResponse1 obErrorResponse1 = errorResponse.getBody();
        assertThat(obErrorResponse1.getCode()).isEqualTo("OBRI.Consent.Store.Error");
        assertThat(obErrorResponse1.getId()).isNotNull();
        assertThat(obErrorResponse1.getErrors()).hasSize(1);
        final OBError1 obError = obErrorResponse1.getErrors().get(0);
        assertThat(obError.getErrorCode()).isEqualTo("INVALID_API_VERSION");
        assertThat(obError.getMessage()).isEqualTo("INVALID_API_VERSION for consentId: " + consentId
                + ", additional details: Consent created using API version: 4.0.0 cannot be accessed using version: "
                + controllerVersion.getCanonicalVersion());
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

    protected <T> ResponseEntity<T> deleteConsent(String consentId, String apiClientId, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + consentId, HttpMethod.DELETE,
                new HttpEntity<>(createConsentStoreApiRequiredHeaders(apiClientId)),
                responseClass);
    }
}
