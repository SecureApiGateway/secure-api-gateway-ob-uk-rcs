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
package com.forgerock.sapi.gateway.rcs.consent.store.repo;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

/**
 * Component which controls the IntentTypes that are enabled to use the Consent Store module.
 * This allows IntentTypes to be migrated to use this module on a per type basis.
 *
 * To be enabled, an IntentType must have a supported implementation and be configured to be enabled via config.
 */
@Component
public class ConsentStoreEnabledIntentTypes {

    /**
     * IntentTypes enabled to use this module via config
     */
    private final EnumSet<IntentType> consentStoreEnabledIntentTypes;

    /**
     * IntentTypes which this module has implemented support for
     */
    private final EnumSet<IntentType> implementedIntentTypes = EnumSet.of(IntentType.PAYMENT_DOMESTIC_CONSENT);

    public ConsentStoreEnabledIntentTypes(EnumSet<IntentType> consentStoreEnabledIntentTypes) {
        this.consentStoreEnabledIntentTypes = consentStoreEnabledIntentTypes;
    }

    public boolean isIntentTypeSupported(IntentType intentType) {
        return consentStoreEnabledIntentTypes.contains(intentType) && implementedIntentTypes.contains(intentType);
    }
}
