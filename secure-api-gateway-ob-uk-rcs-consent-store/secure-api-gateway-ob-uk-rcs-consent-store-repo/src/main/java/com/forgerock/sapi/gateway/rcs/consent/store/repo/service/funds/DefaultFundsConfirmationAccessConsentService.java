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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds;

import org.springframework.stereotype.Service;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.funds.FundsConfirmationConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.funds.FundsConfirmationConsentRepository;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Service
public class DefaultFundsConfirmationAccessConsentService extends BaseConsentService<FundsConfirmationConsentEntity, FundsConfirmationAuthoriseConsentArgs> implements FundsConfirmationConsentService {

    private final String revokedStatus;

    public DefaultFundsConfirmationAccessConsentService(FundsConfirmationConsentRepository repo) {
        super(repo, IntentType.FUNDS_CONFIRMATION_CONSENT::generateIntentId, FundsConfirmationConsentStateModel.getInstance());
        revokedStatus = FundsConfirmationConsentStateModel.getInstance().getRevokedConsentStatus();
    }

    @Override
    protected void addConsentSpecificAuthorisationData(FundsConfirmationConsentEntity consent, FundsConfirmationAuthoriseConsentArgs authoriseConsentArgs) {
        consent.setAuthorisedDebtorAccountId(authoriseConsentArgs.getAuthorisedDebtorAccountId());
    }
}
