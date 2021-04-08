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
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_REQUEST_INVALID;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.ConsentDetailsRequestTestDataFactory.*;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.assertThat;
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
    @Mock
    private DomesticScheduledPaymentConsentDetailsService domesticScheduledPaymentConsentDetailsService;
    @Mock
    private DomesticStandingOrderConsentDetailsService domesticStandingOrderConsentDetailsService;
    @Mock
    private InternationalPaymentConsentDetailsService internationalPaymentConsentDetailsService;
    @Mock
    private InternationalScheduledPaymentConsentDetailsService internationalScheduledPaymentConsentDetailsService;
    @Mock
    private InternationalStandingOrderConsentDetailsService internationalStandingOrderConsentDetailsService;
    @Mock
    private FilePaymentConsentDetailsService filePaymentConsentDetailsService;
    @Mock
    private FundsConfirmationConsentDetailsService fundsConfirmationConsentDetailsService;
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
    public void shouldGetDomesticScheduledPaymentConsentDetails() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidDomesticScheduledPaymentConsentDetailsRequestBuilder().build();
        DomesticScheduledPaymentConsentDetails paymentConsentDetails = DomesticScheduledPaymentConsentDetails.builder().build();
        given(domesticScheduledPaymentConsentDetailsService.getConsentDetails(request)).willReturn(paymentConsentDetails);

        // When
        ConsentDetails consentDetails = consentDetailsServiceDelegate.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        verify(domesticScheduledPaymentConsentDetailsService).getConsentDetails(request);
    }

    @Test
    public void shouldGetDomesticStandingOrderConsentDetails() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidDomesticStandingOrderConsentDetailsRequestBuilder().build();
        DomesticStandingOrderConsentDetails paymentConsentDetails = DomesticStandingOrderConsentDetails.builder().build();
        given(domesticStandingOrderConsentDetailsService.getConsentDetails(request)).willReturn(paymentConsentDetails);

        // When
        ConsentDetails consentDetails = consentDetailsServiceDelegate.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        verify(domesticStandingOrderConsentDetailsService).getConsentDetails(request);
    }

    @Test
    public void shouldGetInternationalPaymentConsentDetails() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidInternationalPaymentConsentDetailsRequestBuilder().build();
        InternationalPaymentConsentDetails paymentConsentDetails = InternationalPaymentConsentDetails.builder().build();
        given(internationalPaymentConsentDetailsService.getConsentDetails(request)).willReturn(paymentConsentDetails);

        // When
        ConsentDetails consentDetails = consentDetailsServiceDelegate.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        verify(internationalPaymentConsentDetailsService).getConsentDetails(request);
    }

    @Test
    public void shouldGetInternationalScheduledPaymentConsentDetails() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidInternationalScheduledPaymentConsentDetailsRequestBuilder().build();
        InternationalScheduledPaymentConsentDetails paymentConsentDetails = InternationalScheduledPaymentConsentDetails.builder().build();
        given(internationalScheduledPaymentConsentDetailsService.getConsentDetails(request)).willReturn(paymentConsentDetails);

        // When
        ConsentDetails consentDetails = consentDetailsServiceDelegate.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        verify(internationalScheduledPaymentConsentDetailsService).getConsentDetails(request);
    }

    @Test
    public void shouldGetInternationalStandingOrderConsentDetails() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidInternationalStandingOrderConsentDetailsRequestBuilder().build();
        InternationalStandingOrderConsentDetails paymentConsentDetails = InternationalStandingOrderConsentDetails.builder().build();
        given(internationalStandingOrderConsentDetailsService.getConsentDetails(request)).willReturn(paymentConsentDetails);

        // When
        ConsentDetails consentDetails = consentDetailsServiceDelegate.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        verify(internationalStandingOrderConsentDetailsService).getConsentDetails(request);
    }

    @Test
    public void shouldGetFilePaymentConsentDetails() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidFilePaymentConsentDetailsRequestBuilder().build();
        FilePaymentConsentDetails paymentConsentDetails = FilePaymentConsentDetails.builder().build();
        given(filePaymentConsentDetailsService.getConsentDetails(request)).willReturn(paymentConsentDetails);

        // When
        ConsentDetails consentDetails = consentDetailsServiceDelegate.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        verify(filePaymentConsentDetailsService).getConsentDetails(request);
    }

    @Test
    public void shouldGetFundsConfirmationConsentDetails() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidFundsConfirmationConsentDetailsRequestBuilder().build();
        FundsConfirmationConsentDetails paymentConsentDetails = FundsConfirmationConsentDetails.builder().build();
        given(fundsConfirmationConsentDetailsService.getConsentDetails(request)).willReturn(paymentConsentDetails);

        // When
        ConsentDetails consentDetails = consentDetailsServiceDelegate.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        verify(fundsConfirmationConsentDetailsService).getConsentDetails(request);
    }

    @Test
    public void shouldFailToGetConsentDetailsGivenUnsupportedIntentType() {
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