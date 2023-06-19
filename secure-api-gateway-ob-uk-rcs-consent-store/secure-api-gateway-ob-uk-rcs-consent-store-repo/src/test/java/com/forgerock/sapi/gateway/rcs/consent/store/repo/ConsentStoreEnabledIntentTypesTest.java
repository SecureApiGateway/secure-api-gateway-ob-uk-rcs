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
        // Try to enable Accounts which are not yet implemented
        final EnumSet<IntentType> intentsToEnable = EnumSet.of(IntentType.PAYMENT_DOMESTIC_CONSENT, IntentType.ACCOUNT_ACCESS_CONSENT);
        final ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes = new ConsentStoreEnabledIntentTypes(intentsToEnable);

        assertTrue(consentStoreEnabledIntentTypes.isIntentTypeSupported(IntentType.PAYMENT_DOMESTIC_CONSENT));
        assertFalse(consentStoreEnabledIntentTypes.isIntentTypeSupported(IntentType.ACCOUNT_ACCESS_CONSENT)); // Not implemented so cannot be enabled
    }

}