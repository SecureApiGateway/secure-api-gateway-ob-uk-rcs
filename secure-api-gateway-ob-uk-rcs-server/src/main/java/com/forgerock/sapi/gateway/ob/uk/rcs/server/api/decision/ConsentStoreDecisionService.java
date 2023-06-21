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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreEnabledIntentTypes;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.AuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Component
public class ConsentStoreDecisionService {

    @FunctionalInterface
    interface AuthoriseConsentArgsFactory {
        AuthoriseConsentArgs createAuthoriseConsentArgs(String intentId, String apiClientId, String resourceOwnerId,
                                                        ConsentDecisionDeserialized consentDecision);
    }

    private final ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes;

    private final EnumMap<IntentType, Pair<ConsentService<?,?>, AuthoriseConsentArgsFactory>> consentServicesAndAuthoriseConsentArgsFactories;


    public ConsentStoreDecisionService(ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes,
                                       DomesticPaymentConsentService domesticPaymentConsentService,
                                       AccountAccessConsentService accountAccessConsentService) {

        this.consentStoreEnabledIntentTypes = Objects.requireNonNull(consentStoreEnabledIntentTypes, "consentStoreEnabledIntentTypes must be provided");

        consentServicesAndAuthoriseConsentArgsFactories = new EnumMap<>(IntentType.class);
        consentServicesAndAuthoriseConsentArgsFactories.put(IntentType.ACCOUNT_ACCESS_CONSENT, Pair.of(Objects.requireNonNull(accountAccessConsentService, "accountAccessConsentService must be provided"),
                                                                                                       this::buildAccountAccessAuthoriseConsentArgs));
        consentServicesAndAuthoriseConsentArgsFactories.put(IntentType.PAYMENT_DOMESTIC_CONSENT,  Pair.of(Objects.requireNonNull(domesticPaymentConsentService, "domesticPaymentConsentService must be provided"),
                                                                                                          this::buildDomesticPaymentIntentAuthoriseConsentArgs));

    }

    public boolean isIntentTypeSupported(IntentType intentType) {
        return consentStoreEnabledIntentTypes.isIntentTypeSupported(intentType);
    }

    private void checkIntentTypeIsSupported(IntentType intentType) {
        if (!isIntentTypeSupported(intentType)) {
            throw new IllegalStateException(intentType + " not supported");
        }
    }

    public void authoriseConsent(IntentType intentType, String intentId, String apiClientId, String resourceOwnerId, ConsentDecisionDeserialized consentDecision) {
        checkIntentTypeIsSupported(intentType);

        final Pair<ConsentService<?,?>, AuthoriseConsentArgsFactory> consentServiceAndAuthoriseConsentArgsFactory = consentServicesAndAuthoriseConsentArgsFactories.get(intentType);
        if (consentServiceAndAuthoriseConsentArgsFactory == null) {
            throw new IllegalStateException(intentType + " is not supported");
        }

        final ConsentService consentService = consentServiceAndAuthoriseConsentArgsFactory.getFirst();
        final AuthoriseConsentArgsFactory authoriseConsentArgsFactory = consentServiceAndAuthoriseConsentArgsFactory.getSecond();
        final AuthoriseConsentArgs authoriseConsentArgs = authoriseConsentArgsFactory.createAuthoriseConsentArgs(intentId, apiClientId, resourceOwnerId, consentDecision);
        consentService.authoriseConsent(authoriseConsentArgs);
    }

    private AccountAccessAuthoriseConsentArgs buildAccountAccessAuthoriseConsentArgs(String intentId, String apiClientId, String resourceOwnerId, ConsentDecisionDeserialized consentDecision) {
        final List<String> authorisedAccountIds = consentDecision.getAccountIds();
        if (authorisedAccountIds == null || authorisedAccountIds.isEmpty()) {
            // TODO use better exception
            throw new IllegalStateException("consentDecision is missing authorisedAccountIds");
        }
        final AccountAccessAuthoriseConsentArgs authoriseConsentArgs = new AccountAccessAuthoriseConsentArgs(intentId, apiClientId, resourceOwnerId, authorisedAccountIds);
        return authoriseConsentArgs;
    }

    private DomesticPaymentAuthoriseConsentArgs buildDomesticPaymentIntentAuthoriseConsentArgs(String intentId, String apiClientId, String resourceOwnerId, ConsentDecisionDeserialized consentDecision) {
        final FRFinancialAccount debtorAccount = consentDecision.getDebtorAccount();
        if (debtorAccount == null || debtorAccount.getAccountId() == null) {
            // TODO use better exception
            throw new IllegalStateException("consentDecision is missing debtorAccount details");
        }
        final DomesticPaymentAuthoriseConsentArgs authoriseConsentArgs = new DomesticPaymentAuthoriseConsentArgs(intentId, apiClientId, resourceOwnerId, debtorAccount.getAccountId());
        return authoriseConsentArgs;
    }

    public void rejectConsent(IntentType intentType, String intentId, String apiClientId, String resourceOwnerId) {
        checkIntentTypeIsSupported(intentType);

        final Pair<ConsentService<?,?>, AuthoriseConsentArgsFactory>  consentServiceAndAuthoriseConsentArgsFactory = consentServicesAndAuthoriseConsentArgsFactories.get(intentType);
        if (consentServiceAndAuthoriseConsentArgsFactory == null) {
            throw new IllegalStateException(intentType + " is not supported");
        }
        final ConsentService consentService = consentServiceAndAuthoriseConsentArgsFactory.getFirst();
        consentService.rejectConsent(intentId, apiClientId, resourceOwnerId);
    }
}
