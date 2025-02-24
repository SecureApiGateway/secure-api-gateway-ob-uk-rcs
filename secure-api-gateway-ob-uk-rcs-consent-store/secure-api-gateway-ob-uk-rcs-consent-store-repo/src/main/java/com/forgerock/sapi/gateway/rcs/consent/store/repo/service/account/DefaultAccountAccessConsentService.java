/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

public class DefaultAccountAccessConsentService extends BaseConsentService<AccountAccessConsentEntity, AccountAccessAuthoriseConsentArgs> implements AccountAccessConsentService {

    private final String revokedStatus;

    public DefaultAccountAccessConsentService(MongoRepository<AccountAccessConsentEntity, String> repo) {
        super(repo, IntentType.ACCOUNT_ACCESS_CONSENT::generateIntentId, AccountAccessConsentStateModel.getInstance());
        revokedStatus = AccountAccessConsentStateModel.getInstance().getRevokedConsentStatus();
    }

    @Override
    protected void addConsentSpecificAuthorisationData(AccountAccessConsentEntity consent, AccountAccessAuthoriseConsentArgs authoriseConsentArgs) {
        consent.setAuthorisedAccountIds(authoriseConsentArgs.getAuthorisedAccountIds());
    }
}