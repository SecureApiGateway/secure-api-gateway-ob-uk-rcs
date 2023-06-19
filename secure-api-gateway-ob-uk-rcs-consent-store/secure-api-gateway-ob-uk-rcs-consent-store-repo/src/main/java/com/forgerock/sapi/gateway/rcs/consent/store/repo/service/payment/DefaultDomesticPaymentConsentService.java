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

import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.DomesticPaymentConsentRepository;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;

@Service
public class DefaultDomesticPaymentConsentService extends BaseConsentService<DomesticPaymentConsentEntity, DomesticPaymentAuthoriseConsentArgs> implements DomesticPaymentConsentService {

    private static final MultiValueMap<String, String> DOMESTIC_PAYMENT_CONSENT_STATE_TRANSITIONS;

    static {
        DOMESTIC_PAYMENT_CONSENT_STATE_TRANSITIONS = new LinkedMultiValueMap<>();
        DOMESTIC_PAYMENT_CONSENT_STATE_TRANSITIONS.addAll(StatusEnum.AWAITINGAUTHORISATION.toString(), List.of(StatusEnum.AUTHORISED.toString(), StatusEnum.REJECTED.toString()));
        DOMESTIC_PAYMENT_CONSENT_STATE_TRANSITIONS.addAll(StatusEnum.AUTHORISED.toString(), List.of(StatusEnum.CONSUMED.toString(), StatusEnum.REJECTED.toString()));
    }

    private final DomesticPaymentConsentRepository repo;

    public DefaultDomesticPaymentConsentService(DomesticPaymentConsentRepository repo) {
        super(repo, IntentType.PAYMENT_DOMESTIC_CONSENT::generateIntentId, DOMESTIC_PAYMENT_CONSENT_STATE_TRANSITIONS,
                StatusEnum.AUTHORISED.toString(), StatusEnum.REJECTED.toString(), StatusEnum.REJECTED.toString());

        this.repo = repo;
    }

    @Override
    public DomesticPaymentConsentEntity createConsent(DomesticPaymentConsentEntity consent) {
        final Optional<DomesticPaymentConsentEntity> consentMatchingIdempotencyData = repo.findByIdempotencyData(consent.getApiClientId(), consent.getIdempotencyKey(), DateTime.now());
        // TODO ifPresent then test that requests match
        return consentMatchingIdempotencyData.orElseGet(() -> super.createConsent(consent));
    }

    @Override
    public DomesticPaymentConsentEntity consumeConsent(String consentId, String apiClientId) {
        final DomesticPaymentConsentEntity consent = getConsent(consentId, apiClientId);
        final String consumedStatus = StatusEnum.CONSUMED.toString();
        validateStateTransition(consent, consumedStatus);
        consent.setStatus(consumedStatus);
        return repo.save(consent);
    }

    @Override
    protected void addConsentSpecificAuthorisationData(DomesticPaymentConsentEntity consent, DomesticPaymentAuthoriseConsentArgs authoriseConsentArgs) {
        consent.setAuthorisedDebtorAccountId(authoriseConsentArgs.getAuthorisedDebtorAccountId());
    }
}
