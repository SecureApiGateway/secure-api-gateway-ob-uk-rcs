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

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

class ConsentStoreEnabledIntentTypesTest {

    @Test
    public void testStoreWithPaymentDomesticConsentEnabled() {
        final EnumSet<IntentType> enabledIntentTypes = EnumSet.of(IntentType.PAYMENT_DOMESTIC_CONSENT);
        final ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes = new ConsentStoreEnabledIntentTypes(enabledIntentTypes);
        assertTrue(consentStoreEnabledIntentTypes.isIntentTypeSupported(IntentType.PAYMENT_DOMESTIC_CONSENT));

        for (IntentType disabledIntentType : EnumSet.complementOf(enabledIntentTypes)) {
            assertFalse(consentStoreEnabledIntentTypes.isIntentTypeSupported(disabledIntentType));
        }
    }

    @Test
    public void testStoreWithAllIntentTypesDisabled() {
        final ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes = new ConsentStoreEnabledIntentTypes(EnumSet.noneOf(IntentType.class));
        for (IntentType intentType : IntentType.values()) {
            assertFalse(consentStoreEnabledIntentTypes.isIntentTypeSupported(intentType));
        }
    }

    // NOTE: this test will need to be updated over time as the set of implemented types increases
    @Test
    public void testThatYouCannotEnableAnIntentTypeThatHasNotBeenImplemented() {
        // Try to enable FUNDS_CONFIRMATION_CONSENT which are not yet implemented
        final EnumSet<IntentType> intentsToEnable = EnumSet.of(IntentType.PAYMENT_DOMESTIC_CONSENT, IntentType.FUNDS_CONFIRMATION_CONSENT);
        final ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes = new ConsentStoreEnabledIntentTypes(intentsToEnable);

        assertTrue(consentStoreEnabledIntentTypes.isIntentTypeSupported(IntentType.PAYMENT_DOMESTIC_CONSENT));
        assertFalse(consentStoreEnabledIntentTypes.isIntentTypeSupported(IntentType.FUNDS_CONFIRMATION_CONSENT)); // Not implemented so cannot be enabled
    }

}