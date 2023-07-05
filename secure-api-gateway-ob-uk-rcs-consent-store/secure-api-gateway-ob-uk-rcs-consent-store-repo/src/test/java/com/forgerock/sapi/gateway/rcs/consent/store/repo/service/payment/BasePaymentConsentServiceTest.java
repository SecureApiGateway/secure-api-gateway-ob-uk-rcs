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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import javax.validation.ConstraintViolationException;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.BasePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentStateModel;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;

public abstract class BasePaymentConsentServiceTest<T extends BasePaymentConsentEntity<?>> extends BaseConsentServiceTest<T, PaymentAuthoriseConsentArgs> {

    @Override
    protected ConsentStateModel getConsentStateModel() {
        return PaymentConsentStateModel.getInstance();
    }

    protected PaymentConsentService<T, PaymentAuthoriseConsentArgs> getPaymentConsentService() {
        return (PaymentConsentService<T, PaymentAuthoriseConsentArgs>) getConsentServiceToTest();
    }

    @Override
    protected PaymentAuthoriseConsentArgs getAuthoriseConsentArgs(String consentId, String resourceOwnerId, String apiClientId) {
        return new PaymentAuthoriseConsentArgs(consentId, apiClientId, resourceOwnerId, "debtor-acc-444");
    }

    @Override
    protected void validateConsentSpecificAuthorisationFields(T authorisedConsent, PaymentAuthoriseConsentArgs authorisationArgs) {
        assertThat(authorisedConsent.getAuthorisedDebtorAccountId()).isEqualTo(authorisationArgs.getAuthorisedDebtorAccountId());
    }

    protected T getConsentInStateToConsume() {
        final T consentToAuthorise = getConsentInStateToAuthoriseOrReject();
        return consentService.authoriseConsent(getAuthoriseConsentArgs(consentToAuthorise.getId(), TEST_RESOURCE_OWNER, consentToAuthorise.getApiClientId()));
    }

    @Test
    void createConsentShouldBeIdempotent() {
        final String idempotencyKey = "key-1";
        final DateTime idempotencyKeyExpiry = DateTime.now().plusDays(1);

        T firstCreateResponse = null;
        for (int i = 0 ; i < 10; i++) {
            final T validConsentEntity = getValidConsentEntity();
            validConsentEntity.setIdempotencyKey(idempotencyKey);
            validConsentEntity.setIdempotencyKeyExpiration(idempotencyKeyExpiry);
            final T consentResponse = getPaymentConsentService().createConsent(validConsentEntity);
            if (firstCreateResponse == null) {
                firstCreateResponse = consentResponse;
            } else {
                assertThat(consentResponse).usingRecursiveComparison().isEqualTo(firstCreateResponse);
            }
        }
    }

    @Test
    void consumeConsent() {
        final T consentInStateToConsume = getConsentInStateToConsume();
        final T consumedConsent = getPaymentConsentService().consumeConsent(consentInStateToConsume.getId(), consentInStateToConsume.getApiClientId());
        assertThat(consumedConsent.getStatus()).isEqualTo(StatusEnum.CONSUMED.toString());
    }

    @Test
    void failToConsumeConsentAwaitingAuthorisation() {
        final T persistedConsent = getConsentInStateToAuthoriseOrReject();
        final ConsentStoreException consentStoreException = Assertions.assertThrows(ConsentStoreException.class, () -> getPaymentConsentService().consumeConsent(persistedConsent.getId(), persistedConsent.getApiClientId()));
        assertThat(consentStoreException.getConsentId()).isEqualTo(persistedConsent.getId());
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.INVALID_STATE_TRANSITION);
        assertThat(consentStoreException.getMessage()).contains("cannot transition from consentStatus: AwaitingAuthorisation to status: Consumed");
    }

    @Test
    void failToAuthoriseConsentMissingDebtorAccountId() {
        final T persistedConsent = getConsentInStateToAuthoriseOrReject();
        final ConstraintViolationException ex = Assertions.assertThrows(ConstraintViolationException.class,
                () -> getPaymentConsentService().authoriseConsent(new PaymentAuthoriseConsentArgs(persistedConsent.getId(), persistedConsent.getApiClientId(), "user-1234", null)));
        assertThat(ex.getMessage()).isEqualTo("authoriseConsent.arg0.authorisedDebtorAccountId: must not be null");
    }

    @Test
    void testConsentCannotBeReAuthenticated() {
        final T consent = getValidConsentEntity();
        consent.setStatus(getConsentStateModel().getAuthorisedConsentStatus());
        assertThat(consentService.canTransitionToAuthorisedState(consent)).isFalse();
    }

    @Override
    protected void validateConsentSpecificFields(T expected, T actual) {
        assertThat(actual.getIdempotencyKey()).isEqualTo(expected.getIdempotencyKey());
        assertThat(actual.getIdempotencyKeyExpiration()).isEqualTo(expected.getIdempotencyKeyExpiration());
        assertThat(actual.getCharges()).isEqualTo(expected.getCharges());
    }
}
