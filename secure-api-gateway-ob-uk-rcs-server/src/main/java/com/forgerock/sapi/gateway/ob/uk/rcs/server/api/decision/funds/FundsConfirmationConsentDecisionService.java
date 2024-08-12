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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.funds;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.BaseConsentDecisionService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.funds.FundsConfirmationConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.FundsConfirmationAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.FundsConfirmationConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Component
@DependsOn({"internalConsentServices"})
public class FundsConfirmationConsentDecisionService extends BaseConsentDecisionService<FundsConfirmationConsentEntity, FundsConfirmationAuthoriseConsentArgs> {
    public FundsConfirmationConsentDecisionService(@Qualifier("internalFundsConfirmationConsentService") FundsConfirmationConsentService consentService) {
        super(IntentType.FUNDS_CONFIRMATION_CONSENT, consentService);
    }

    @Override
    protected FundsConfirmationAuthoriseConsentArgs buildAuthoriseConsentArgs(
            String intentId,
            String apiClientId,
            String resourceOwnerId,
            ConsentDecisionDeserialized consentDecision
    ) {
        final FRFinancialAccount debtorAccount = consentDecision.getDebtorAccount();
        if (debtorAccount == null || debtorAccount.getAccountId() == null) {
            throw new ConsentStoreException(ConsentStoreException.ErrorType.INVALID_CONSENT_DECISION, intentId, "consentDecision is missing authorisedAccountId");
        }
        return new FundsConfirmationAuthoriseConsentArgs(intentId, apiClientId, resourceOwnerId, debtorAccount.getAccountId());
    }
}
