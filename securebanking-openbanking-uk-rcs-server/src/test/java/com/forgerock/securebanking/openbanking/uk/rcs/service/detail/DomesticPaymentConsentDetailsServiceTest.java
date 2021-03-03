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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRBankAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRDomesticPaymentConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticDataInitiation;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticPaymentConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.DomesticPaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.exception.InvalidConsentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.FRAccountIdentifierTestDataFactory.aValidFRAccountIdentifier2;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRBankAccountWithBalanceTestDataFactory.aValidFRBankAccountWithBalance;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccountBuilder;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.payment.FRDomesticPaymentConsentTestDataFactory.aValidFRDomesticPaymentConsentBuilder;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.tpp.TppTestDataFactory.aValidTppBuilder;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.ConsentDetailsRequestTestDataFactory.aValidDomesticPaymentConsentDetailsRequest;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.ConsentDetailsRequestTestDataFactory.aValidDomesticPaymentConsentDetailsRequestBuilder;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link DomesticPaymentConsentDetailsService}.
 */
@ExtendWith(MockitoExtension.class)
public class DomesticPaymentConsentDetailsServiceTest {
    @Mock
    private DomesticPaymentConsentService paymentConsentService;
    @Mock
    private TppService tppService;
    @InjectMocks
    private DomesticPaymentConsentDetailsService consentDetailsService;

    @Test
    public void shouldTestSomethingGiven() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidDomesticPaymentConsentDetailsRequest();
        FRDomesticPaymentConsent consent = aValidFRDomesticPaymentConsentBuilder()
                .pispId(request.getClientId())
                .build();
        Tpp tpp = aValidTppBuilder()
                .clientId(request.getClientId())
                .build();
        given(paymentConsentService.getConsent(request.getIntentId())).willReturn(consent);
        given(tppService.getTpp(consent.getPispId())).willReturn(Optional.of(tpp));

        // When
        DomesticPaymentConsentDetails consentDetails = consentDetailsService.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        assertThat(consent.getUserId()).isEqualTo(request.getUsername());
        FRWriteDomesticDataInitiation initiation = (FRWriteDomesticDataInitiation) consent.getInitiation();
        assertThat(consentDetails.getInstructedAmount()).isEqualTo(initiation.getInstructedAmount());
        assertThat(consentDetails.getAccounts()).isEqualTo(request.getAccounts());
        assertThat(consentDetails.getUsername()).isEqualTo(request.getUsername());
        assertThat(consentDetails.getMerchantName()).isEqualTo(consent.getPispName());
        assertThat(consentDetails.getClientId()).isEqualTo(request.getClientId());
        verify(paymentConsentService).updateConsent(consent);
    }

    @Test
    public void shouldFailToGetConsentDetailsGivenPaymentConsentNotFound() {
        // Given
        ConsentDetailsRequest request = aValidDomesticPaymentConsentDetailsRequest();
        given(paymentConsentService.getConsent(request.getIntentId())).willReturn(null);

        // When
        OBErrorException e = catchThrowableOfType(() -> consentDetailsService.getConsentDetails(request), OBErrorException.class);

        // Then
        assertThat(e.getObriErrorType()).isEqualTo(PAYMENT_CONSENT_NOT_FOUND);
    }

    @Test
    public void shouldFailToGetConsentDetailsGivenDebtorAccountDoesNotBelongToUser() {
        // Given
        FRBankAccountWithBalance accountWithBalance = aValidFRBankAccountWithBalance();
        accountWithBalance.setAccount(aValidFRFinancialAccountBuilder()
                .accounts(List.of(aValidFRAccountIdentifier2()))
                .build());
        ConsentDetailsRequest request = aValidDomesticPaymentConsentDetailsRequestBuilder()
                .accounts(List.of(accountWithBalance))
                .build();
        FRDomesticPaymentConsent consent = aValidFRDomesticPaymentConsentBuilder()
                .build();
        given(paymentConsentService.getConsent(request.getIntentId())).willReturn(consent);

        // When
        InvalidConsentException e = catchThrowableOfType(() -> consentDetailsService.getConsentDetails(request), InvalidConsentException.class);

        // Then
        assertThat(e.getErrorType()).isEqualTo(RCS_CONSENT_REQUEST_DEBTOR_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void shouldFailToGetConsentDetailsGivenTppNotFound() {
        // Given
        ConsentDetailsRequest request = aValidDomesticPaymentConsentDetailsRequest();
        FRDomesticPaymentConsent consent = aValidFRDomesticPaymentConsentBuilder()
                .pispId(request.getClientId())
                .build();
        given(paymentConsentService.getConsent(request.getIntentId())).willReturn(consent);
        given(tppService.getTpp(consent.getPispId())).willReturn(Optional.empty());

        // When
        InvalidConsentException e = catchThrowableOfType(() -> consentDetailsService.getConsentDetails(request), InvalidConsentException.class);

        // Then
        assertThat(e.getErrorType()).isEqualTo(RCS_CONSENT_REQUEST_NOT_FOUND_TPP);
    }

    @Test
    public void shouldFailToGetConsentDetailsGivenTppDidNotCreateConsent() {
        // Given
        ConsentDetailsRequest request = aValidDomesticPaymentConsentDetailsRequest();
        FRDomesticPaymentConsent consent = aValidFRDomesticPaymentConsentBuilder()
                .pispId(request.getClientId())
                .build();
        Tpp tpp = aValidTppBuilder()
                .build();
        given(paymentConsentService.getConsent(request.getIntentId())).willReturn(consent);
        given(tppService.getTpp(consent.getPispId())).willReturn(Optional.of(tpp));

        // When
        OBErrorException e = catchThrowableOfType(() -> consentDetailsService.getConsentDetails(request), OBErrorException.class);

        // Then
        assertThat(e.getObriErrorType()).isEqualTo(RCS_CONSENT_REQUEST_INVALID_PAYMENT_REQUEST);
    }
}