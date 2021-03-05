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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRDomesticPaymentConsent;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.rs.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRConsentStatusCode.AUTHORISED;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRConsentStatusCode.REJECTED;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountTestDataFactory.aValidFRAccountBuilder;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.payment.FRDomesticPaymentConsentTestDataFactory.aValidFRDomesticPaymentConsent;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit test for {@link PaymentConsentDecisionUpdater}.
 */
@ExtendWith(MockitoExtension.class)
public class PaymentConsentDecisionUpdaterTest {

    @Mock
    private AccountService accountService;

    @Mock
    private PaymentConsentService<FRDomesticPaymentConsent> paymentConsentService;

    @InjectMocks
    private PaymentConsentDecisionUpdater decisionUpdater;

    @Test
    public void shouldApplyUpdateGivenConsentIsApproved() throws OBErrorException {
        // Given
        String userId = randomUUID().toString();
        String accountId = randomUUID().toString();
        FRDomesticPaymentConsent paymentConsent = aValidFRDomesticPaymentConsent();
        FRAccount frAccount = aValidFRAccountBuilder()
                .id(accountId)
                .userId(userId)
                .account(aValidFRFinancialAccount())
                .build();
        given(accountService.getAccounts(userId)).willReturn(List.of(frAccount));

        // When
        decisionUpdater.applyUpdate(userId, accountId, true, paymentConsentService::updateConsent, paymentConsent);

        // Then
        assertThat(paymentConsent.getStatus()).isEqualTo(AUTHORISED);
    }

    @Test
    public void shouldApplyUpdateGivenConsentIsRejected() throws OBErrorException {
        // Given
        String userId = randomUUID().toString();
        String accountId = randomUUID().toString();
        FRDomesticPaymentConsent paymentConsent = aValidFRDomesticPaymentConsent();

        // When
        decisionUpdater.applyUpdate(userId, accountId, false, paymentConsentService::updateConsent, paymentConsent);

        // Then
        assertThat(paymentConsent.getStatus()).isEqualTo(REJECTED);
    }

    @Test
    public void shouldFailToApplyUpdateGivenMissingAccountId() throws OBErrorException {
        // Given
        String userId = randomUUID().toString();
        String accountId = "";
        FRDomesticPaymentConsent paymentConsent = aValidFRDomesticPaymentConsent();

        // When
        IllegalArgumentException e = catchThrowableOfType(() -> decisionUpdater.applyUpdate(userId, accountId, true,
                paymentConsentService::updateConsent, paymentConsent), IllegalArgumentException.class);

        // Then
        assertThat(e.getMessage()).isEqualTo("Missing account id");
    }
}