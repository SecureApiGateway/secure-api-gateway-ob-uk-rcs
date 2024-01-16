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

import java.util.Collection;
import java.util.EnumMap;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreEnabledIntentTypes;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Component
public class ConsentStoreDecisionServiceRegistry {

    private final ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes;

    private final EnumMap<IntentType, ConsentStoreDecisionService> intentTypeDecisionServices;


    public ConsentStoreDecisionServiceRegistry(ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes,
                                               Collection<ConsentStoreDecisionService> consentStoreDecisionServices) {

        this.consentStoreEnabledIntentTypes = Objects.requireNonNull(consentStoreEnabledIntentTypes,
                "consentStoreEnabledIntentTypes must be provided");
        this.intentTypeDecisionServices = new EnumMap<>(IntentType.class);
        for (ConsentStoreDecisionService consentStoreDecisionService : consentStoreDecisionServices) {
            intentTypeDecisionServices.put(consentStoreDecisionService.getSupportedIntentType(), consentStoreDecisionService);
        }
    }

    public boolean isIntentTypeSupported(IntentType intentType) {
        return consentStoreEnabledIntentTypes.isIntentTypeSupported(intentType)
                && intentTypeDecisionServices.containsKey(intentType);
    }

    private ConsentStoreDecisionService getConsentStoreDecisionService(IntentType intentType) {
        if (!isIntentTypeSupported(intentType)) {
            throw new IllegalStateException(intentType + " not supported");
        }
        return intentTypeDecisionServices.get(intentType);
    }

    public void authoriseConsent(IntentType intentType, String intentId, String apiClientId, String resourceOwnerId,
                                 ConsentDecisionDeserialized consentDecision) {

        getConsentStoreDecisionService(intentType).authoriseConsent(intentId, apiClientId, resourceOwnerId, consentDecision);
    }

    public void rejectConsent(IntentType intentType, String intentId, String apiClientId, String resourceOwnerId) {
        getConsentStoreDecisionService(intentType).rejectConsent(intentId, apiClientId, resourceOwnerId);
    }

}
