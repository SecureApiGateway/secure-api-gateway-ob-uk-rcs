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
package com.forgerock.securebanking.openbanking.uk.rcs.service.detail;

import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticPaymentConsentDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_REQUEST_INVALID;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.ConsentDetailsRequestTestDataFactory.*;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link ConsentDetailsServiceDelegate}.
 */
@ExtendWith(MockitoExtension.class)
public class ConsentDetailsServiceDelegateTest {
    @Mock
    private AccountConsentDetailsService accountConsentDetailsService;
    @Mock
    private DomesticPaymentConsentDetailsService domesticPaymentConsentDetailsService;
    @InjectMocks
    private ConsentDetailsServiceDelegate consentDetailsServiceDelegate;

    @Test
    public void shouldGetAccountAccessConsentDetails() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidAccountAccessConsentDetailsRequest();
        AccountsConsentDetails accountsConsentDetails = AccountsConsentDetails.builder().build();
        given(accountConsentDetailsService.getConsentDetails(request)).willReturn(accountsConsentDetails);

        // When
        ConsentDetails consentDetails = consentDetailsServiceDelegate.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        verify(accountConsentDetailsService).getConsentDetails(request);
    }

    @Test
    public void shouldGetDomesticPaymentConsentDetails() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidDomesticPaymentConsentDetailsRequest();
        DomesticPaymentConsentDetails paymentConsentDetails = DomesticPaymentConsentDetails.builder().build();
        given(domesticPaymentConsentDetailsService.getConsentDetails(request)).willReturn(paymentConsentDetails);

        // When
        ConsentDetails consentDetails = consentDetailsServiceDelegate.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        verify(domesticPaymentConsentDetailsService).getConsentDetails(request);
    }

    @Test
    public void shouldFailToGetConsentDetailsGivenUnsupportedIntentType() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidAccountAccessConsentDetailsRequestBuilder()
                .intentId("AR_" + randomUUID().toString())
                .build();

        // When
        OBErrorException e = catchThrowableOfType(() -> consentDetailsServiceDelegate.getConsentDetails(request), OBErrorException.class);

        // Then
        assertThat(e.getObriErrorType()).isEqualTo(RCS_CONSENT_REQUEST_INVALID);
    }
}