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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;

@Service
public class DefaultAccountAccessConsentService extends BaseConsentService<AccountAccessConsentEntity, AccountAccessAuthoriseConsentArgs> implements AccountAccessConsentService {


    private static final MultiValueMap<String, String> ACCOUNT_ACCESS_CONSENT_STATE_TRANSITIONS;

    static {
        ACCOUNT_ACCESS_CONSENT_STATE_TRANSITIONS = new LinkedMultiValueMap<>();
        ACCOUNT_ACCESS_CONSENT_STATE_TRANSITIONS.addAll(OBExternalRequestStatus1Code.AWAITINGAUTHORISATION.toString(),
                List.of(OBExternalRequestStatus1Code.AUTHORISED.toString(), OBExternalRequestStatus1Code.REJECTED.toString()));
    }


    public DefaultAccountAccessConsentService(MongoRepository<AccountAccessConsentEntity, String> repo) {
        super(repo, IntentType.ACCOUNT_ACCESS_CONSENT::generateIntentId, ACCOUNT_ACCESS_CONSENT_STATE_TRANSITIONS,
              OBExternalRequestStatus1Code.AUTHORISED.toString(), OBExternalRequestStatus1Code.REJECTED.toString(),
              OBExternalRequestStatus1Code.REJECTED.toString());
    }

    @Override
    protected void addConsentSpecificAuthorisationData(AccountAccessConsentEntity consent, AccountAccessAuthoriseConsentArgs authoriseConsentArgs) {
        consent.setAuthorisedAccountIds(authoriseConsentArgs.getAuthorisedAccountIds());
    }
}