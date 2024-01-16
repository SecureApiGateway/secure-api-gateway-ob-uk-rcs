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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreEnabledIntentTypes;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;


class ConsentStoreDetailsServiceRegistryTest {

    private static ConsentStoreDetailsServiceRegistry createRegistryWithNoIntentTypesEnabledAndNoDetailsServices() {
        return new ConsentStoreDetailsServiceRegistry(new ConsentStoreEnabledIntentTypes(EnumSet.noneOf(IntentType.class)), List.of());
    }

    @Test
    void testIsIntentTypeSupportedAllIntentTypesDisabled() {
        final ConsentStoreDetailsServiceRegistry registry = createRegistryWithNoIntentTypesEnabledAndNoDetailsServices();
        for (IntentType intentType : IntentType.values()) {
            assertFalse(registry.isIntentTypeSupported(intentType));
        }
    }

    @Test
    void testGetDetailsFailsIfIntentTypeDisabled() {
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                createRegistryWithNoIntentTypesEnabledAndNoDetailsServices().getDetailsFromConsentStore(IntentType.ACCOUNT_ACCESS_CONSENT,
                        new ConsentClientDetailsRequest(null, null, null, null)));

        assertEquals("ACCOUNT_ACCESS_CONSENT support not currently implemented in Consent Store module", ex.getMessage());
    }

    @Test
    void testIntentTypeIsNotSupportedIfEnabledButNotImpl() {
        final IntentType enabledIntentType = IntentType.PAYMENT_DOMESTIC_CONSENT;
        final ConsentStoreDetailsServiceRegistry registry = new ConsentStoreDetailsServiceRegistry(new ConsentStoreEnabledIntentTypes(EnumSet.of(enabledIntentType)), List.of());
        assertEquals(false, registry.isIntentTypeSupported(enabledIntentType));
    }

    @Test
    void testDispatchesToDetailsService() throws ExceptionClient {
        final ConsentStoreDetailsService accountDetailsService = mock(ConsentStoreDetailsService.class);
        given(accountDetailsService.getSupportedIntentType()).willReturn(IntentType.ACCOUNT_ACCESS_CONSENT);
        final ConsentStoreDetailsService domesticPaymentDetailsService = mock(ConsentStoreDetailsService.class);
        given(domesticPaymentDetailsService.getSupportedIntentType()).willReturn(IntentType.PAYMENT_DOMESTIC_CONSENT);

        final Pair<IntentType, ConsentStoreDetailsService>[] intentTypesToServices = new Pair[] {
                    Pair.of(IntentType.ACCOUNT_ACCESS_CONSENT, accountDetailsService),
                    Pair.of(IntentType.PAYMENT_DOMESTIC_CONSENT, domesticPaymentDetailsService)
        };

        final ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes = new ConsentStoreEnabledIntentTypes(
                EnumSet.of(IntentType.ACCOUNT_ACCESS_CONSENT, IntentType.PAYMENT_DOMESTIC_CONSENT));
        final ConsentStoreDetailsServiceRegistry registry = new ConsentStoreDetailsServiceRegistry(consentStoreEnabledIntentTypes,
                List.of(accountDetailsService, domesticPaymentDetailsService));


        for (Pair<IntentType, ConsentStoreDetailsService> intentTypeToService : intentTypesToServices) {
            final ConsentStoreDetailsService detailsService = intentTypeToService.getSecond();
            verify(detailsService).getSupportedIntentType();
            final IntentType intentType = intentTypeToService.getFirst();
            assertEquals(true, registry.isIntentTypeSupported(intentType));
            final ConsentClientDetailsRequest consentClientRequest = mock(ConsentClientDetailsRequest.class);
            registry.getDetailsFromConsentStore(intentType, consentClientRequest);
            verify(detailsService).getDetailsFromConsentStore(eq(consentClientRequest));
        }

        for (Pair<IntentType, ConsentStoreDetailsService> intentTypeToService : intentTypesToServices) {
            verifyNoMoreInteractions(intentTypeToService.getSecond());
        }
    }

}