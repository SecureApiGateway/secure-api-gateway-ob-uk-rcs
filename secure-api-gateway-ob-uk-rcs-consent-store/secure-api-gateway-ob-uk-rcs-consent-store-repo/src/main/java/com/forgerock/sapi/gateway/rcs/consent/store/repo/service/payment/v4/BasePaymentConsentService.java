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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.v4;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.BasePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.payment.PaymentConsentRepository;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentConsentService;
import org.joda.time.DateTime;
import uk.org.openbanking.datamodel.v4.payment.OBPaymentConsentStatus;

import java.util.Optional;
import java.util.function.Supplier;

public class BasePaymentConsentService<T extends BasePaymentConsentEntity<?>, A extends PaymentAuthoriseConsentArgs> extends BaseConsentService<T, A> implements PaymentConsentService<T, A> {

    protected BasePaymentConsentService(PaymentConsentRepository<T> repo, Supplier<String> idGenerator) {
        this(repo, idGenerator, PaymentConsentStateModel.getInstance());
    }

    protected BasePaymentConsentService(PaymentConsentRepository<T> repo, Supplier<String> idGenerator,
                                        ConsentStateModel consentStateModel) {

        super(repo, idGenerator, consentStateModel);
    }

    private PaymentConsentRepository<T> getRepo() {
        return (PaymentConsentRepository<T>) repo;
    }

    @Override
    public T createConsent(T consent) {
        final Optional<T> consentMatchingIdempotencyData = getRepo().findByIdempotencyData(consent.getApiClientId(), consent.getIdempotencyKey(), DateTime.now());
        if (consentMatchingIdempotencyData.isPresent()) {
            final T existingConsent = consentMatchingIdempotencyData.get();
            if (!existingConsent.getRequestObj().equals(consent.getRequestObj())) {
                throw new ConsentStoreException(ErrorType.IDEMPOTENCY_ERROR, existingConsent.getId(),
                        "The provided Idempotency Key: '" + consent.getIdempotencyKey() + "' header matched a previous request but the request body has been changed.");
            }
        }
        return consentMatchingIdempotencyData.orElseGet(() -> super.createConsent(consent));
    }

    public T consumeConsent(String consentId, String apiClientId) {
        final T consent = getConsent(consentId, apiClientId);
        final String consumedStatus = OBPaymentConsentStatus.COND.getValue();
        validateStateTransition(consent, consumedStatus);
        consent.setStatus(consumedStatus);
        return repo.save(consent);
    }

    @Override
    protected void addConsentSpecificAuthorisationData(T consent, A authoriseConsentArgs) {
        consent.setAuthorisedDebtorAccountId(authoriseConsentArgs.getAuthorisedDebtorAccountId());
    }
}
