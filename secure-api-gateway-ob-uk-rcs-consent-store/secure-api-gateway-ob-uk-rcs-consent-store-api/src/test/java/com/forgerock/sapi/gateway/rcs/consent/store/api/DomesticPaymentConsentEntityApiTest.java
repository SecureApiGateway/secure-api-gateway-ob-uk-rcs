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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.AuthoriseDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.ConsumeDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.RejectDomesticPaymentConsentRequest;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class DomesticPaymentConsentEntityApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String apiBaseUrl;

    @PostConstruct
    public void postConstruct() {
        apiBaseUrl = "http://localhost:" + port + "/consent/store/v3.1.10/domestic-payment-consents";
    }


    @Test
    public void failToGetConsentDoesNotExist() {
        final ResponseEntity<DomesticPaymentConsent> getResponse = makeGetRequest("unknown-consnt", "client-123", DomesticPaymentConsent.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void failToCreateConsentRequestMissingRequiredFields() {
        final CreateDomesticPaymentConsentRequest invalidRequest = new CreateDomesticPaymentConsentRequest();
        final ResponseEntity<Map> createConsentResponse = makePostRequest(invalidRequest, Map.class);
        assertThat(createConsentResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // TODO validate error response data
    }

    @Test
    public void createConsent() {
        final String apiClientId = "abcd-1234-efgh-5678";

        final DomesticPaymentConsent consent = createConsent(apiClientId);

        final ResponseEntity<DomesticPaymentConsent> getConsentResponseEntity = makeGetRequest(consent.getId(), consent.getApiClientId(), DomesticPaymentConsent.class);
        assertThat(getConsentResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        final DomesticPaymentConsent getConsentResponse = getConsentResponseEntity.getBody();
        assertThat(getConsentResponse).usingRecursiveComparison().isEqualTo(consent);
    }

    private DomesticPaymentConsent createConsent(String apiClientId) {
        return createConsent(apiClientId, UUID.randomUUID().toString());
    }

    private DomesticPaymentConsent createConsent(String apiClientId, String idempotencyKey) {
        final CreateDomesticPaymentConsentRequest createDomesticPaymentConsentRequest = new CreateDomesticPaymentConsentRequest();
        final OBWriteDomesticConsent4 paymentConsent = OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4();
        createDomesticPaymentConsentRequest.setConsentRequest(paymentConsent);
        createDomesticPaymentConsentRequest.setApiClientId(apiClientId);
        createDomesticPaymentConsentRequest.setIdempotencyKey(idempotencyKey);
        createDomesticPaymentConsentRequest.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));

        return createConsent(createDomesticPaymentConsentRequest);
    }

    private DomesticPaymentConsent createConsent(CreateDomesticPaymentConsentRequest createDomesticPaymentConsentRequest) {
        final ResponseEntity<DomesticPaymentConsent> consentResponseEntity = makePostRequest(createDomesticPaymentConsentRequest, DomesticPaymentConsent.class);
        assertThat(consentResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        final DomesticPaymentConsent consent = consentResponseEntity.getBody();
        assertThat(consent.getId()).isNotEmpty();
        assertThat(consent.getStatus()).isEqualTo(StatusEnum.AWAITINGAUTHORISATION.toString());
        assertThat(consent.getApiClientId()).isEqualTo(createDomesticPaymentConsentRequest.getApiClientId());
        assertThat(consent.getRequestObj()).isEqualTo(createDomesticPaymentConsentRequest.getConsentRequest());

        // TODO more validation

        return consent;
    }

    @Test
    public void testIdempotentBehaviourForSameKeyAndApiClient() {
        final String apiClientId = "client-1";
        final String idempotencyKey = UUID.randomUUID().toString();

        final CreateDomesticPaymentConsentRequest createDomesticPaymentConsentRequest = new CreateDomesticPaymentConsentRequest();
        final OBWriteDomesticConsent4 paymentConsent = OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4();
        createDomesticPaymentConsentRequest.setConsentRequest(paymentConsent);
        createDomesticPaymentConsentRequest.setApiClientId(apiClientId);
        createDomesticPaymentConsentRequest.setIdempotencyKey(idempotencyKey);
        createDomesticPaymentConsentRequest.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));

        final DomesticPaymentConsent firstConsent = createConsent(createDomesticPaymentConsentRequest);
        for (int i = 0 ; i < 5; i++) {
            assertThat(createConsent(createDomesticPaymentConsentRequest)).usingRecursiveComparison().isEqualTo(firstConsent);
        }
    }

    @Test
    public void testSameIdempotentKeyCanBeUsedByDifferentClients() {
        final String idempotencyKey = UUID.randomUUID().toString();
        final CreateDomesticPaymentConsentRequest client1CreateRequest = new CreateDomesticPaymentConsentRequest();
        client1CreateRequest.setConsentRequest(OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4());
        client1CreateRequest.setApiClientId("client-1");
        client1CreateRequest.setIdempotencyKey(idempotencyKey);
        client1CreateRequest.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));

        final CreateDomesticPaymentConsentRequest client2CreateRequest = new CreateDomesticPaymentConsentRequest();
        client2CreateRequest.setConsentRequest(OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4());
        client2CreateRequest.setApiClientId("client-2");
        client2CreateRequest.setIdempotencyKey(idempotencyKey);
        client2CreateRequest.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));

        final DomesticPaymentConsent client1Consent = createConsent(client1CreateRequest);
        final DomesticPaymentConsent client2Consent = createConsent(client2CreateRequest);
        assertThat(client1Consent.getId()).isNotEqualTo(client2Consent.getId());
        assertThat(client1Consent.getApiClientId()).isEqualTo("client-1");
        assertThat(client2Consent.getApiClientId()).isEqualTo("client-2");
    }

    @Test
    public void failToGetConsentForDifferentApiClient() {
        final DomesticPaymentConsent client1Consent = createConsent("client-1");
        final ResponseEntity<OBErrorResponse1> getConsentResponseEntity = makeGetRequest(client1Consent.getId(), "client-2", OBErrorResponse1.class);
        assertThat(getConsentResponseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        final OBErrorResponse1 errorResponse = getConsentResponseEntity.getBody();
        assertThat(errorResponse.getCode()).isEqualTo("OBRI.Consent.Store.Error");
        assertThat(errorResponse.getId()).isNotNull(); // TODO test with x-fapi-interaction-id
        assertThat(errorResponse.getMessage()).isEqualTo(HttpStatus.UNAUTHORIZED.name());
        assertThat(errorResponse.getErrors()).hasSize(1);
        final OBError1 obError = errorResponse.getErrors().get(0);
        assertThat(obError.getErrorCode()).isEqualTo("NOT_AUTHORISED");
        assertThat(obError.getMessage()).isEqualTo("NOT_AUTHORISED for consentId: " + client1Consent.getId());
    }

    @Test
    public void authoriseConsent() {
        final String apiClientId = "client-1";
        final String resourceOwnerId = "psu4test";
        final String debtorAccountId = "acc-123456";
        final DomesticPaymentConsent consent = createConsent(apiClientId);

        final AuthoriseDomesticPaymentConsentRequest authoriseReq = new AuthoriseDomesticPaymentConsentRequest();
        authoriseReq.setConsentId(consent.getId());
        authoriseReq.setApiClientId(consent.getApiClientId());
        authoriseReq.setResourceOwnerId(resourceOwnerId);
        authoriseReq.setAuthorisedDebtorAccountId(debtorAccountId);

        final ResponseEntity<DomesticPaymentConsent> authoriseConsentResponse = authoriseConsent(authoriseReq, DomesticPaymentConsent.class);
        assertThat(authoriseConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        final DomesticPaymentConsent authorisedConsent = authoriseConsentResponse.getBody();
        assertThat(authorisedConsent.getStatus()).isEqualTo(StatusEnum.AUTHORISED.toString());
        assertThat(authorisedConsent.getResourceOwnerId()).isEqualTo(resourceOwnerId);
        assertThat(authorisedConsent.getAuthorisedDebtorAccountId()).isEqualTo(debtorAccountId);

        // TODO more validation

    }

    @Test
    public void failToAuthoriseConsentMissingRequiredFields() {

    }

    @Test
    public void failToAuthoriseConsentCreatedByDifferentApiClient() {

    }

    @Test
    public void rejectConsent() {
        final String apiClientId = "client-1";
        final String resourceOwnerId = "psu4test";
        final DomesticPaymentConsent consent = createConsent(apiClientId);

        final RejectDomesticPaymentConsentRequest rejectRequest = new RejectDomesticPaymentConsentRequest();
        rejectRequest.setConsentId(consent.getId());
        rejectRequest.setResourceOwnerId(resourceOwnerId);
        rejectRequest.setApiClientId(apiClientId);

        final ResponseEntity<DomesticPaymentConsent> rejectResponse = rejectConsent(rejectRequest, DomesticPaymentConsent.class);
        assertThat(rejectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rejectResponse.getBody().getStatus()).isEqualTo(StatusEnum.REJECTED.toString());
        // TODO more validations
    }

    @Test
    public void failToRejectConsentCreatedByDifferentApiClient() {

    }

    @Test
    public void consumeConsent() {
        final String apiClientId = "client-1";
        final String resourceOwnerId = "psu4test";
        final String debtorAccountId = "acc-123456";
        final DomesticPaymentConsent consent = createConsent(apiClientId);

        final AuthoriseDomesticPaymentConsentRequest authoriseReq = new AuthoriseDomesticPaymentConsentRequest();
        authoriseReq.setConsentId(consent.getId());
        authoriseReq.setApiClientId(consent.getApiClientId());
        authoriseReq.setResourceOwnerId(resourceOwnerId);
        authoriseReq.setAuthorisedDebtorAccountId(debtorAccountId);

        authoriseConsent(authoriseReq, DomesticPaymentConsent.class);

        final ConsumeDomesticPaymentConsentRequest consumeRequest = new ConsumeDomesticPaymentConsentRequest();
        consumeRequest.setConsentId(consent.getId());
        consumeRequest.setApiClientId(consent.getApiClientId());

        final ResponseEntity<DomesticPaymentConsent> consumeResponse = consumeConsent(consumeRequest, DomesticPaymentConsent.class);
        assertThat(consumeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(consumeResponse.getBody().getStatus()).isEqualTo(StatusEnum.CONSUMED.toString());
    }

    @Test
    public void failToConsumeConsentCreatedByDifferentApiClient() {

    }

    private HttpHeaders createHeaders(String apiClientId) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-client-id", apiClientId);
        return headers;
    }

    private <T> ResponseEntity<T> makeGetRequest(String consentId, String apiClientId, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + consentId,
                                    HttpMethod.GET,
                                    new HttpEntity<>(createHeaders(apiClientId)),
                                    responseClass);
    }

    private <T> ResponseEntity<T> makePostRequest(CreateDomesticPaymentConsentRequest createConsentRequest, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl, HttpMethod.POST,
                                     new HttpEntity<>(createConsentRequest, createHeaders(createConsentRequest.getApiClientId())),
                                     responseClass);
    }

    private <T> ResponseEntity<T> authoriseConsent(AuthoriseDomesticPaymentConsentRequest authoriseReq, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + authoriseReq.getConsentId() + "/authorise" , HttpMethod.POST,
                                     new HttpEntity<>(authoriseReq, createHeaders(authoriseReq.getApiClientId())),
                                     responseClass);
    }

    private <T> ResponseEntity<T> rejectConsent(RejectDomesticPaymentConsentRequest rejectRequest, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + rejectRequest.getConsentId() + "/reject" , HttpMethod.POST,
                new HttpEntity<>(rejectRequest, createHeaders(rejectRequest.getApiClientId())),
                responseClass);
    }

    private <T> ResponseEntity<T> consumeConsent(ConsumeDomesticPaymentConsentRequest consumeRequest, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + consumeRequest.getConsentId() + "/consume" , HttpMethod.POST,
                new HttpEntity<>(consumeRequest, createHeaders(consumeRequest.getApiClientId())),
                responseClass);
    }

}