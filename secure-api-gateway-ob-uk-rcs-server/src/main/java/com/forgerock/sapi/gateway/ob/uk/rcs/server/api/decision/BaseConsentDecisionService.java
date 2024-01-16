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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision;

import java.util.Objects;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.BaseConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.AuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

public abstract class BaseConsentDecisionService<T extends BaseConsentEntity, A extends AuthoriseConsentArgs> implements ConsentStoreDecisionService {
    private final IntentType supportedIntentType;

    protected final ConsentService<T, A> consentService;

    public BaseConsentDecisionService(IntentType supportedIntentType, ConsentService<T, A> consentService) {
        this.supportedIntentType = Objects.requireNonNull(supportedIntentType, "supportedIntentType must be provided");
        this.consentService = Objects.requireNonNull(consentService, "consentService must be provided");
    }

    protected abstract A buildAuthoriseConsentArgs(String intentId, String apiClientId, String resourceOwnerId,
                                                   ConsentDecisionDeserialized consentDecision);

    @Override
    public void authoriseConsent(String intentId, String apiClientId, String resourceOwnerId, ConsentDecisionDeserialized consentDecision) {
        consentService.authoriseConsent(buildAuthoriseConsentArgs(intentId, apiClientId, resourceOwnerId, consentDecision));
    }

    @Override
    public void rejectConsent(String intentId, String apiClientId, String resourceOwnerId) {
        consentService.rejectConsent(intentId, apiClientId, resourceOwnerId);
    }

    @Override
    public IntentType getSupportedIntentType() {
        return supportedIntentType;
    }

}
