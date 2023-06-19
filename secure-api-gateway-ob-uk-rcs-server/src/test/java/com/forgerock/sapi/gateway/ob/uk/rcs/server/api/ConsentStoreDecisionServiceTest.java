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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreEnabledIntentTypes;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@ExtendWith(MockitoExtension.class)
class ConsentStoreDecisionServiceTest {

    private static final String TEST_API_CLIENT_ID = "api-client-1";
    private static final String TEST_RESOURCE_OWNER_ID = "user-123456";
    private static final String TEST_AUTHORISED_ACC_ID = "authorised-acc-1";
    @Mock
    private ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes;

    @Mock
    private DomesticPaymentConsentService domesticPaymentConsentService;

    @InjectMocks
    private ConsentStoreDecisionService consentStoreDecisionService;

    @Test
    public void testUnableToSubmitDecisionForConsentTypeNotEnabled() {
        final IntentType intentType = IntentType.PAYMENT_DOMESTIC_CONSENT;

        assertEquals("PAYMENT_DOMESTIC_CONSENT not supported", assertThrows(IllegalStateException.class,
                () -> consentStoreDecisionService.authoriseConsent(intentType, intentType.generateIntentId(), TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_ACC_ID)).getMessage());
        assertEquals("PAYMENT_DOMESTIC_CONSENT not supported", assertThrows(IllegalStateException.class,
                () -> consentStoreDecisionService.rejectConsent(intentType, intentType.generateIntentId(), TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID)).getMessage());
    }

    @Test
    public void testSubmitDomesticPaymentAuthoriseDecision() {
        final IntentType paymentDomesticConsent = IntentType.PAYMENT_DOMESTIC_CONSENT;
        given(consentStoreEnabledIntentTypes.isIntentTypeSupported(eq(paymentDomesticConsent))).willReturn(Boolean.TRUE);

        final String intentId = paymentDomesticConsent.generateIntentId();

        consentStoreDecisionService.authoriseConsent(paymentDomesticConsent, intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_ACC_ID);

        verify(domesticPaymentConsentService).authoriseConsent(refEq(new DomesticPaymentAuthoriseConsentArgs(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_ACC_ID)));
        verifyNoMoreInteractions(domesticPaymentConsentService);
    }

    @Test
    public void testSubmitDomesticPaymentRejectDecision() {
        final IntentType paymentDomesticConsent = IntentType.PAYMENT_DOMESTIC_CONSENT;
        given(consentStoreEnabledIntentTypes.isIntentTypeSupported(eq(paymentDomesticConsent))).willReturn(Boolean.TRUE);

        final String intentId = paymentDomesticConsent.generateIntentId();

        consentStoreDecisionService.rejectConsent(paymentDomesticConsent, intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID);

        verify(domesticPaymentConsentService).rejectConsent(eq(intentId), eq(TEST_API_CLIENT_ID), eq(TEST_RESOURCE_OWNER_ID));
        verifyNoMoreInteractions(domesticPaymentConsentService);
    }
}