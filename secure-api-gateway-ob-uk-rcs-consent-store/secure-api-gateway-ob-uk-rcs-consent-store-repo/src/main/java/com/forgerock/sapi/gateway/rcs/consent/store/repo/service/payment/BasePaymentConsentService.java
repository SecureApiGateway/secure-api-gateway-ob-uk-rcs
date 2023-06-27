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

import java.util.Optional;
import java.util.function.Supplier;

import org.joda.time.DateTime;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.BasePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.payment.PaymentConsentRepository;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;

public class BasePaymentConsentService<T extends BasePaymentConsentEntity<?>, A extends PaymentAuthoriseConsentArgs> extends BaseConsentService<T, A> implements PaymentConsentService<T, A> {

    public BasePaymentConsentService(PaymentConsentRepository<T> repo, Supplier<String> idGenerator) {
        super(repo, idGenerator, PaymentConsentStateModel.getInstance());
    }

    private PaymentConsentRepository<T> getRepo() {
        return (PaymentConsentRepository<T>) repo;
    }

    @Override
    public T createConsent(T consent) {
        final Optional<T> consentMatchingIdempotencyData = getRepo().findByIdempotencyData(consent.getApiClientId(), consent.getIdempotencyKey(), DateTime.now());
        // TODO ifPresent then test that requests match
        return consentMatchingIdempotencyData.orElseGet(() -> super.createConsent(consent));
    }

    public T consumeConsent(String consentId, String apiClientId) {
        final T consent = getConsent(consentId, apiClientId);
        final String consumedStatus = StatusEnum.CONSUMED.toString();
        validateStateTransition(consent, consumedStatus);
        consent.setStatus(consumedStatus);
        return repo.save(consent);
    }

    @Override
    protected void addConsentSpecificAuthorisationData(T consent, A authoriseConsentArgs) {
        consent.setAuthorisedDebtorAccountId(authoriseConsentArgs.getAuthorisedDebtorAccountId());
    }
}
