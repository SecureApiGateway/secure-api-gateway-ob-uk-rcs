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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domestic.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.ApiTestUtils.createConsentStoreApiRequiredHeaders;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domestic.v3_1_10.DomesticPaymentConsentValidationHelpers.validateConsumedConsent;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.AuthoriseDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.ConsumeDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.BaseControllerTest;

import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory;


public class DomesticPaymentConsentApiControllerTest extends BaseControllerTest<DomesticPaymentConsent, CreateDomesticPaymentConsentRequest, AuthoriseDomesticPaymentConsentRequest> {

    private static final String TEST_DEBTOR_ACC_ID = "acc-12345";

    public DomesticPaymentConsentApiControllerTest() {
        super(DomesticPaymentConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "domestic-payment-consents";
    }

    @Override
    protected CreateDomesticPaymentConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateDomesticPaymentConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateDomesticPaymentConsentRequest buildCreateDomesticPaymentConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateDomesticPaymentConsentRequest createDomesticPaymentConsentRequest = new CreateDomesticPaymentConsentRequest();
        final OBWriteDomesticConsent4 paymentConsent = OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4();
        createDomesticPaymentConsentRequest.setConsentRequest(paymentConsent);
        createDomesticPaymentConsentRequest.setApiClientId(apiClientId);
        createDomesticPaymentConsentRequest.setIdempotencyKey(idempotencyKey);
        return createDomesticPaymentConsentRequest;
    }

    @Override
    protected void validateCreateConsentAgainstCreateRequest(DomesticPaymentConsent consent, CreateDomesticPaymentConsentRequest createDomesticPaymentConsentRequest) {
        DomesticPaymentConsentValidationHelpers.validateCreateConsentAgainstCreateRequest(consent, createDomesticPaymentConsentRequest);
    }

    @Override
    protected AuthoriseDomesticPaymentConsentRequest buildAuthoriseConsentRequest(DomesticPaymentConsent consent, String resourceOwnerId) {
        final AuthoriseDomesticPaymentConsentRequest authoriseReq = new AuthoriseDomesticPaymentConsentRequest();
        authoriseReq.setConsentId(consent.getId());
        authoriseReq.setApiClientId(consent.getApiClientId());
        authoriseReq.setResourceOwnerId(resourceOwnerId);
        authoriseReq.setAuthorisedDebtorAccountId(TEST_DEBTOR_ACC_ID);
        return authoriseReq;
    }

    @Override
    protected void validateAuthorisedConsent(DomesticPaymentConsent authorisedConsent, AuthoriseDomesticPaymentConsentRequest authoriseConsentReq, DomesticPaymentConsent originalConsent) {
        DomesticPaymentConsentValidationHelpers.validateAuthorisedConsent(authorisedConsent, authoriseConsentReq, originalConsent);
    }

    @Override
    protected void validateRejectedConsent(DomesticPaymentConsent rejectedConsent, RejectConsentRequest rejectConsentRequest, DomesticPaymentConsent originalConsent) {
        DomesticPaymentConsentValidationHelpers.validateRejectedConsent(rejectedConsent, rejectConsentRequest, originalConsent);
    }

    @Test
    public void testIdempotentBehaviourForSameKeyAndApiClient() {
        final String apiClientId = "client-1";
        final String idempotencyKey = UUID.randomUUID().toString();

        final CreateDomesticPaymentConsentRequest createDomesticPaymentConsentRequest = buildCreateDomesticPaymentConsentRequest(apiClientId, idempotencyKey);

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

        final CreateDomesticPaymentConsentRequest client2CreateRequest = new CreateDomesticPaymentConsentRequest();
        client2CreateRequest.setConsentRequest(OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4());
        client2CreateRequest.setApiClientId("client-2");
        client2CreateRequest.setIdempotencyKey(idempotencyKey);

        final DomesticPaymentConsent client1Consent = createConsent(client1CreateRequest);
        final DomesticPaymentConsent client2Consent = createConsent(client2CreateRequest);
        assertThat(client1Consent.getId()).isNotEqualTo(client2Consent.getId());
        assertThat(client1Consent.getApiClientId()).isEqualTo("client-1");
        assertThat(client2Consent.getApiClientId()).isEqualTo("client-2");
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

        final ResponseEntity<DomesticPaymentConsent> authorisedConsentResponse = authoriseConsent(authoriseReq, DomesticPaymentConsent.class);
        final DomesticPaymentConsent authorisedConsent = authorisedConsentResponse.getBody();

        final ConsumeDomesticPaymentConsentRequest consumeRequest = new ConsumeDomesticPaymentConsentRequest();
        consumeRequest.setConsentId(consent.getId());
        consumeRequest.setApiClientId(consent.getApiClientId());

        final ResponseEntity<DomesticPaymentConsent> consumeResponse = consumeConsent(consumeRequest, DomesticPaymentConsent.class);
        assertThat(consumeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        final DomesticPaymentConsent consumedConsent = consumeResponse.getBody();
        validateConsumedConsent(consumedConsent, authorisedConsent);
    }

    @Test
    public void failToConsumeConsentCreatedByDifferentApiClient() {
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
        consumeRequest.setApiClientId("another-api-client");

        final ResponseEntity<OBErrorResponse1> consumeResponse = consumeConsent(consumeRequest, OBErrorResponse1.class);
        validateInvalidPermissionsErrorResponse(consent.getId(), consumeResponse);
    }

    private <T> ResponseEntity<T> consumeConsent(ConsumeDomesticPaymentConsentRequest consumeRequest, Class<T> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + consumeRequest.getConsentId() + "/consume" , HttpMethod.POST,
                new HttpEntity<>(consumeRequest, createConsentStoreApiRequiredHeaders(consumeRequest.getApiClientId())),
                responseClass);
    }

}