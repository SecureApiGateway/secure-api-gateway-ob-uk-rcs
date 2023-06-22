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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.account;

import java.util.List;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.BaseConsentDecisionService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Component
public class AccountAccessConsentDecisionService extends BaseConsentDecisionService<AccountAccessConsentEntity, AccountAccessAuthoriseConsentArgs> {

    public AccountAccessConsentDecisionService(AccountAccessConsentService accountAccessConsentService) {
        super(IntentType.ACCOUNT_ACCESS_CONSENT, accountAccessConsentService);
    }

    @Override
    protected AccountAccessAuthoriseConsentArgs buildAuthoriseConsentArgs(String intentId, String apiClientId, String resourceOwnerId, ConsentDecisionDeserialized consentDecision) {
        final List<String> authorisedAccountIds = consentDecision.getAccountIds();
        if (authorisedAccountIds == null || authorisedAccountIds.isEmpty()) {
            throw new ConsentStoreException(ErrorType.INVALID_CONSENT_DECISION, intentId, "consentDecision is missing authorisedAccountIds");
        }
        return new AccountAccessAuthoriseConsentArgs(intentId, apiClientId, resourceOwnerId, authorisedAccountIds);
    }
}
