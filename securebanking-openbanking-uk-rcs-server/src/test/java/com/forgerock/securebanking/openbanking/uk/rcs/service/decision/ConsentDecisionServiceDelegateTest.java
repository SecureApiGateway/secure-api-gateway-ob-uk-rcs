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

import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_REQUEST_INVALID;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Unit test for {@link ConsentDecisionServiceDelegate}.
 */
@ExtendWith(MockitoExtension.class)
public class ConsentDecisionServiceDelegateTest {
    @Mock
    private AccountAccessConsentDecisionService accountAccessConsentDecisionService;
    @Mock
    private DomesticPaymentConsentDecisionService domesticPaymentConsentDecisionService;
    @InjectMocks
    private ConsentDecisionServiceDelegate delegate;

    @Test
    public void shouldGetAccountAccessConsentDecisionService() throws OBErrorException {
        // Given
        String intentId = "AAC_1234";

        // When
        ConsentDecisionService consentDecisionService = delegate.getConsentDecisionService(intentId);

        // Then
        assertThat(consentDecisionService).isNotNull();
        assertThat(consentDecisionService).isEqualTo(accountAccessConsentDecisionService);
    }

    @Test
    public void shouldGetDomesticPaymentConsentDecisionService() throws OBErrorException {
        // Given
        String intentId = "PDC_1234";

        // When
        ConsentDecisionService consentDecisionService = delegate.getConsentDecisionService(intentId);

        // Then
        assertThat(consentDecisionService).isNotNull();
        assertThat(consentDecisionService).isEqualTo(domesticPaymentConsentDecisionService);
    }

    @Test
    public void shouldFailToGetConsentDecisionServiceGivenInvalidIntentId() {
        // Given
        String intentId = "INVALID_INTENT";

        // When
        OBErrorException e = catchThrowableOfType(() -> delegate.getConsentDecisionService(intentId), OBErrorException.class);

        // Then
        assertThat(e.getObriErrorType()).isEqualTo(RCS_CONSENT_REQUEST_INVALID);
        assertThat(e.getMessage()).isEqualTo("Invalid Remote consent request token. Reason: Invalid intent ID: 'INVALID_INTENT'");
    }

    @Test
    public void shouldFailToGetConsentDecisionServiceGivenUnsupportedIntentType() {
        // Given
        String intentId = "AR_1234";

        // When
        OBErrorException e = catchThrowableOfType(() -> delegate.getConsentDecisionService(intentId), OBErrorException.class);

        // Then
        assertThat(e.getObriErrorType()).isEqualTo(RCS_CONSENT_REQUEST_INVALID);
        assertThat(e.getMessage()).isEqualTo("Invalid Remote consent request token. Reason: Unsupported intent ID: 'AR_1234'");
    }
}