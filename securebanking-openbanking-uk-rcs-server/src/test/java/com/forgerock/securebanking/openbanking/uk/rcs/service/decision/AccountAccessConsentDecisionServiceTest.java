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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountAccessConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.AccountConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.rs.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalRequestStatusCode.AUTHORISED;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalRequestStatusCode.REJECTED;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountAccessConsentTestDataFactory.aValidFRAccountAccessConsent;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_DECISION_INVALID_ACCOUNT;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link AccountAccessConsentDecisionService}.
 */
@ExtendWith(MockitoExtension.class)
public class AccountAccessConsentDecisionServiceTest {
    @Mock
    private AccountService accountService;
    @Mock
    private AccountConsentService accountConsentService;
    private AccountAccessConsentDecisionService consentDecisionService;

    @BeforeEach
    public void setup() {
        consentDecisionService = new AccountAccessConsentDecisionService(accountService, accountConsentService, new ObjectMapper());
    }

    @Test
    public void shouldApproveConsent() throws OBErrorException {
        // Given
        String intentId = "AAC_1234";
        FRAccountAccessConsent accountAccessConsent = aValidFRAccountAccessConsent();
        given(accountConsentService.getAccountConsent(intentId)).willReturn(accountAccessConsent);
        given(accountService.getAccountsWithBalance(accountAccessConsent.getUserId())).willReturn(List.of(aValidFRAccountWithBalance()));

        // When
        consentDecisionService.processConsentDecision(intentId, consentDecisionSerialised("123456"), true);

        // Then
        assertThat(accountAccessConsent.getStatus()).isEqualTo(AUTHORISED);
        assertThat(accountAccessConsent.getAccountIds()).isEqualTo(List.of("123456"));
        verify(accountConsentService).updateAccountConsent(accountAccessConsent);
    }

    @Test
    public void shouldDeclineConsent() throws OBErrorException {
        // Given
        String intentId = "AAC_1234";
        FRAccountAccessConsent accountAccessConsent = aValidFRAccountAccessConsent();
        given(accountConsentService.getAccountConsent(intentId)).willReturn(accountAccessConsent);

        // When
        consentDecisionService.processConsentDecision(intentId, consentDecisionSerialised("123456"), false);

        // Then
        assertThat(accountAccessConsent.getStatus()).isEqualTo(REJECTED);
        verify(accountConsentService).updateAccountConsent(accountAccessConsent);
    }

    @Test
    public void shouldFailToApproveConsentGivenPsuDoesNotOwnAccount() {
        // Given
        String intentId = "AAC_1234";
        FRAccountAccessConsent accountAccessConsent = aValidFRAccountAccessConsent();
        given(accountConsentService.getAccountConsent(intentId)).willReturn(accountAccessConsent);
        List<FRAccountWithBalance> accountsWithBalances = List.of(aValidFRAccountWithBalance());
        given(accountService.getAccountsWithBalance(accountAccessConsent.getUserId())).willReturn(accountsWithBalances);

        // When
        OBErrorException e = catchThrowableOfType(() ->
                consentDecisionService.processConsentDecision(intentId, consentDecisionSerialised("222222"), true),
                OBErrorException.class);

        // Then
        assertThat(e.getObriErrorType()).isEqualTo(RCS_CONSENT_DECISION_INVALID_ACCOUNT);
    }

    private String consentDecisionSerialised(String accountId) {
        return "{\"consentJwt\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0N" +
                "TY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"," +
                "\"decision\":\"true\"," +
                "\"sharedAccounts\":[\"" + accountId + "\"]}";
    }
}