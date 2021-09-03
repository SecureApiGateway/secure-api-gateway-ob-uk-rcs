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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.AccountConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRAccountAccessConsent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRAccountAccessConsentTestDataFactory.aValidFRAccountAccessConsentBuilder;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.tpp.TppTestDataFactory.aValidTpp;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.ConsentDetailsRequestTestDataFactory.aValidAccountAccessConsentDetailsRequest;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link AccountConsentDetailsService}.
 */
@ExtendWith(MockitoExtension.class)
public class AccountConsentDetailsServiceTest {
    @Mock
    private AccountConsentService accountConsentService;
    @Mock
    private TppService tppService;
    @InjectMocks
    private AccountConsentDetailsService consentDetailsService;

    @Test
    public void shouldGetConsentDetails() throws OBErrorException {
        // Given
        ConsentDetailsRequest request = aValidAccountAccessConsentDetailsRequest();
        FRAccountAccessConsent accessConsent = aValidFRAccountAccessConsentBuilder()
                .clientId(request.getClientId())
                .build();
        Tpp tpp = aValidTpp();
        given(accountConsentService.getAccountConsent(request.getIntentId())).willReturn(accessConsent);
        given(tppService.getTpp(accessConsent.getAispId())).willReturn(Optional.of(tpp));

        // When
        AccountsConsentDetails consentDetails = consentDetailsService.getConsentDetails(request);

        // Then
        assertThat(consentDetails).isNotNull();
        assertThat(consentDetails.getPermissions()).isEqualTo(accessConsent.getPermissions());
        assertThat(consentDetails.getFromTransaction()).isEqualTo(accessConsent.getTransactionFromDateTime());
        assertThat(consentDetails.getToTransaction()).isEqualTo(accessConsent.getTransactionToDateTime());
        assertThat(consentDetails.getAccounts()).isEqualTo(request.getAccounts());
        assertThat(consentDetails.getUsername()).isEqualTo(request.getUsername());
        assertThat(consentDetails.getLogo()).isEqualTo(tpp.getLogoUri());
        assertThat(consentDetails.getClientId()).isEqualTo(request.getClientId());
        assertThat(consentDetails.getAispName()).isEqualTo(accessConsent.getAispName());
        assertThat(consentDetails.getExpiredDate()).isEqualTo(accessConsent.getExpirationDateTime());
        assertThat(accessConsent.getUserId()).isEqualTo(request.getUsername());
        verify(accountConsentService).updateAccountConsent(accessConsent);
    }

    @Test
    public void shouldFailToGetConsentDetailsGivenAccountConsentNotFound() {
        // Given
        ConsentDetailsRequest request = aValidAccountAccessConsentDetailsRequest();
        given(accountConsentService.getAccountConsent(request.getIntentId())).willReturn(null);

        // When
        OBErrorException e = catchThrowableOfType(() -> consentDetailsService.getConsentDetails(request), OBErrorException.class);

        // Then
        assertThat(e.getObriErrorType()).isEqualTo(RCS_CONSENT_REQUEST_UNKNOWN_ACCOUNT_REQUEST);
    }

    @Test
    public void shouldFailToGetConsentDetailsGivenAccountConsentWithDifferentAispId() {
        // Given
        ConsentDetailsRequest request = aValidAccountAccessConsentDetailsRequest();
        FRAccountAccessConsent accessConsent = aValidFRAccountAccessConsentBuilder()
                .build();
        given(accountConsentService.getAccountConsent(request.getIntentId())).willReturn(accessConsent);

        // When
        OBErrorException e = catchThrowableOfType(() -> consentDetailsService.getConsentDetails(request), OBErrorException.class);

        // Then
        assertThat(e.getObriErrorType()).isEqualTo(RCS_CONSENT_REQUEST_INVALID_CONSENT);
    }

    @Test
    public void shouldFailToGetConsentDetailsGivenTppNotFound() {
        // Given
        ConsentDetailsRequest request = aValidAccountAccessConsentDetailsRequest();
        FRAccountAccessConsent accessConsent = aValidFRAccountAccessConsentBuilder()
                .clientId(request.getClientId())
                .build();
        given(accountConsentService.getAccountConsent(request.getIntentId())).willReturn(accessConsent);
        given(tppService.getTpp(accessConsent.getAispId())).willReturn(Optional.empty());

        // When
        OBErrorException e = catchThrowableOfType(() -> consentDetailsService.getConsentDetails(request), OBErrorException.class);

        // Then
        assertThat(e.getObriErrorType()).isEqualTo(RCS_CONSENT_REQUEST_NOT_FOUND_TPP);
    }
}
