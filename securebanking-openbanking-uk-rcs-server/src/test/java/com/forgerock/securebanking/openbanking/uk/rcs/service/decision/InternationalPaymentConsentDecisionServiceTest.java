/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.service.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRInternationalPaymentConsent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.PAYMENT_CONSENT_NOT_FOUND;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRInternationalPaymentConsentTestDataFactory.aValidFRInternationalPaymentConsent;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link InternationalPaymentConsentDecisionService}.
 */
@ExtendWith(MockitoExtension.class)
public class InternationalPaymentConsentDecisionServiceTest {

    @Mock
    private PaymentConsentService paymentConsentService;
    @Mock
    private PaymentConsentDecisionUpdater consentDecisionUpdater;
    private InternationalPaymentConsentDecisionService consentDecisionService;

    @BeforeEach
    public void setup() {
        consentDecisionService = new InternationalPaymentConsentDecisionService(paymentConsentService,
                new ObjectMapper(), consentDecisionUpdater);
    }

    @Test
    public void shouldApproveConsent() throws OBErrorException {
        // Given
        String intentId = "PIC_1234";
        FRInternationalPaymentConsent paymentConsent = aValidFRInternationalPaymentConsent();
        given(paymentConsentService.getConsent(intentId, FRInternationalPaymentConsent.class)).willReturn(paymentConsent);

        // When
        consentDecisionService.processConsentDecision(intentId, consentDecisionSerialized("123456"), true);

        // Then
        verify(consentDecisionUpdater).applyUpdate(anyString(), anyString(), anyBoolean(), any(), any());
    }

    @Test
    public void shouldFailToApproveConsentGivenConsentNotFound() {
        // Given
        String intentId = "PIC_1234";
        FRInternationalPaymentConsent paymentConsent = aValidFRInternationalPaymentConsent();
        given(paymentConsentService.getConsent(intentId, FRInternationalPaymentConsent.class)).willReturn(null);

        // When
        OBErrorException e = catchThrowableOfType(() ->
                consentDecisionService.processConsentDecision(intentId,
                        consentDecisionSerialized("2222"), true), OBErrorException.class);

        // Then
        Assertions.assertThat(e.getObriErrorType()).isEqualTo(PAYMENT_CONSENT_NOT_FOUND);
    }

    private String consentDecisionSerialized(String accountId) {
        String consentDecisionSerialised = "{" +
                "\"consentJwt\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
                "G4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"," +
                "\"decision\":\"true\"," +
                "\"accountId\":\"" + accountId + "\"}";
        return consentDecisionSerialised;
    }
}