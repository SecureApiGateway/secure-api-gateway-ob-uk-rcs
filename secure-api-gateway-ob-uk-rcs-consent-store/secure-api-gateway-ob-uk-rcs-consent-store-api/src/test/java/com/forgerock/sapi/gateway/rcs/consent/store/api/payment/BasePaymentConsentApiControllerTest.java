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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.ApiTestUtils.createConsentStoreApiRequiredHeaders;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.forgerock.sapi.gateway.rcs.consent.store.api.BaseControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.BaseCreatePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.BasePaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;

import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;

public abstract class BasePaymentConsentApiControllerTest<T extends BasePaymentConsent, C extends BaseCreatePaymentConsentRequest> extends BaseControllerTest<T, C, AuthorisePaymentConsentRequest> {

    private static final String TEST_DEBTOR_ACC_ID = "acc-12345";

    public BasePaymentConsentApiControllerTest(Class<T> consentClass) {
        super(consentClass);
    }

    @Override
    protected AuthorisePaymentConsentRequest buildAuthoriseConsentRequest(T consent, String resourceOwnerId) {
        final AuthorisePaymentConsentRequest authoriseReq = new AuthorisePaymentConsentRequest();
        authoriseReq.setConsentId(consent.getId());
        authoriseReq.setApiClientId(consent.getApiClientId());
        authoriseReq.setResourceOwnerId(resourceOwnerId);
        authoriseReq.setAuthorisedDebtorAccountId(TEST_DEBTOR_ACC_ID);
        return authoriseReq;
    }


    @Test
    public void testIdempotentBehaviourForSameKeyAndApiClient() {
        final String apiClientId = "client-1";
        final String idempotencyKey = UUID.randomUUID().toString();

        final C createConsentRequest = buildCreateConsentRequest(apiClientId);
        createConsentRequest.setIdempotencyKey(idempotencyKey);

        final T firstConsent = createConsent(createConsentRequest);
        for (int i = 0 ; i < 5; i++) {
            Assertions.assertThat(createConsent(createConsentRequest)).usingRecursiveComparison().isEqualTo(firstConsent);
        }
    }

    @Test
    public void testSameIdempotentKeyCanBeUsedByDifferentClients() {
        final String idempotencyKey = UUID.randomUUID().toString();
        final C client1CreateRequest = buildCreateConsentRequest("client-1");
        client1CreateRequest.setIdempotencyKey(idempotencyKey);

        final C client2CreateRequest =  buildCreateConsentRequest("client-2");
        client1CreateRequest.setIdempotencyKey(idempotencyKey);

        final T client1Consent = createConsent(client1CreateRequest);
        final T client2Consent = createConsent(client2CreateRequest);
        assertThat(client1Consent.getId()).isNotEqualTo(client2Consent.getId());
        assertThat(client1Consent.getApiClientId()).isEqualTo("client-1");
        assertThat(client2Consent.getApiClientId()).isEqualTo("client-2");
    }

    @Test
    public void consumeConsent() {
        final String apiClientId = "client-1";
        final String resourceOwnerId = "psu4test";
        final String debtorAccountId = "acc-123456";
        final T consent = getConsentInStateToAuthoriseOrReject(apiClientId);

        final AuthorisePaymentConsentRequest authoriseReq = new AuthorisePaymentConsentRequest();
        authoriseReq.setConsentId(consent.getId());
        authoriseReq.setApiClientId(consent.getApiClientId());
        authoriseReq.setResourceOwnerId(resourceOwnerId);
        authoriseReq.setAuthorisedDebtorAccountId(debtorAccountId);

        final ResponseEntity<T> authorisedConsentResponse = authoriseConsent(authoriseReq, consentClass);
        final T authorisedConsent = authorisedConsentResponse.getBody();

        final ConsumePaymentConsentRequest consumeRequest = new ConsumePaymentConsentRequest();
        consumeRequest.setConsentId(consent.getId());
        consumeRequest.setApiClientId(consent.getApiClientId());

        final ResponseEntity<T> consumeResponse = consumeConsent(consumeRequest, consentClass);
        assertThat(consumeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        final T consumedConsent = consumeResponse.getBody();
        validateConsumedConsent(consumedConsent, authorisedConsent);
    }

    @Test
    public void failToConsumeConsentCreatedByDifferentApiClient() {
        final String apiClientId = "client-1";
        final String resourceOwnerId = "psu4test";
        final String debtorAccountId = "acc-123456";
        final T consent = createConsent(apiClientId);

        final AuthorisePaymentConsentRequest authoriseReq = new AuthorisePaymentConsentRequest();
        authoriseReq.setConsentId(consent.getId());
        authoriseReq.setApiClientId(consent.getApiClientId());
        authoriseReq.setResourceOwnerId(resourceOwnerId);
        authoriseReq.setAuthorisedDebtorAccountId(debtorAccountId);

        authoriseConsent(authoriseReq, consentClass);

        final ConsumePaymentConsentRequest consumeRequest = new ConsumePaymentConsentRequest();
        consumeRequest.setConsentId(consent.getId());
        consumeRequest.setApiClientId("another-api-client");

        final ResponseEntity<OBErrorResponse1> consumeResponse = consumeConsent(consumeRequest, OBErrorResponse1.class);
        validateInvalidPermissionsErrorResponse(consent.getId(), consumeResponse);
    }

    @Override
    protected void validateCreateConsentAgainstCreateRequest(T consent, C createConsentRequest) {
        PaymentConsentValidationHelpers.validateCreateConsentAgainstCreateRequest(consent, createConsentRequest, getControllerVersion());
    }

    @Override
    protected void validateAuthorisedConsent(T authorisedConsent, AuthorisePaymentConsentRequest authoriseConsentReq, T originalConsent) {
        PaymentConsentValidationHelpers.validateAuthorisedConsent(authorisedConsent, authoriseConsentReq, originalConsent);
    }

    @Override
    protected void validateRejectedConsent(T rejectedConsent, RejectConsentRequest rejectConsentRequest, T originalConsent) {
        PaymentConsentValidationHelpers.validateRejectedConsent(rejectedConsent, rejectConsentRequest, originalConsent);
    }


    protected void validateConsumedConsent(T consumedConsent, T authorisedConsent) {
        PaymentConsentValidationHelpers.validateConsumedConsent(consumedConsent, authorisedConsent);
    }

    private <R> ResponseEntity<R> consumeConsent(ConsumePaymentConsentRequest consumeRequest, Class<R> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + consumeRequest.getConsentId() + "/consume" , HttpMethod.POST,
                new HttpEntity<>(consumeRequest, createConsentStoreApiRequiredHeaders(consumeRequest.getApiClientId())),
                responseClass);
    }
}
