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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreEnabledIntentTypes;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

/**
 * Registry of {@link ConsentStoreDetailsService} capable of building ConsentDetails objects for specific IntentTypes
 */
@Component
public class ConsentStoreDetailsServiceRegistry {

    private final ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes;

    private final EnumMap<IntentType, ConsentStoreDetailsService<?>> intentTypeDetailsServices;

    public ConsentStoreDetailsServiceRegistry(ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes,
                                              Collection<ConsentStoreDetailsService<?>> consentStoreDetailsServices) {

        this.consentStoreEnabledIntentTypes = Objects.requireNonNull(consentStoreEnabledIntentTypes, "consentStoreEnabledIntentTypes must be provided");
        this.intentTypeDetailsServices = new EnumMap<>(IntentType.class);
        for (ConsentStoreDetailsService<?> consentStoreDetailsService : consentStoreDetailsServices) {
            intentTypeDetailsServices.put(consentStoreDetailsService.getSupportedIntentType(), consentStoreDetailsService);
        }
    }

    public boolean isIntentTypeSupported(IntentType intentType) {
        return consentStoreEnabledIntentTypes.isIntentTypeSupported(intentType) && intentTypeDetailsServices.containsKey(intentType);
    }

    public ConsentDetails getDetailsFromConsentStore(IntentType intentType, ConsentClientDetailsRequest consentClientRequest) throws ExceptionClient {
        if (!isIntentTypeSupported(intentType)) {
            throw new IllegalStateException(intentType + " support not currently implemented in Consent Store module");
        }
        return intentTypeDetailsServices.get(intentType).getDetailsFromConsentStore(consentClientRequest);
    }
}
