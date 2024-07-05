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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.international;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international.DefaultInternationalScheduledPaymentConsentServiceTest.createValidConsentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRAccountIdentifierConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduledDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.InternationalScheduledPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.InternationalScheduledPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international.InternationalScheduledPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.nimbusds.jwt.SignedJWT;

import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2DataInitiationDebtorAccount;

@ExtendWith(MockitoExtension.class)
class InternationalScheduledPaymentConsentDetailsServiceTest extends BasePaymentConsentDetailsServiceTest {

    @Mock
    private InternationalScheduledPaymentConsentService internationalPaymentConsentService;

    @InjectMocks
    private InternationalScheduledPaymentConsentDetailsService consentDetailsService;

    @Test
    void testGetInternationalPaymentDetails() throws ExceptionClient {
        mockApiClientServiceResponse();
        mockAccountServiceGetAccountsWithBalanceResponse();
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final InternationalScheduledPaymentConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);
        given(internationalPaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(internationalPaymentConsentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, null, testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(InternationalScheduledPaymentConsentDetails.class);
        InternationalScheduledPaymentConsentDetails paymentConsentDetails = (InternationalScheduledPaymentConsentDetails) consentDetails;
        assertThat(paymentConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(paymentConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(paymentConsentDetails.getPaymentReference()).isEqualTo("FRESCO-037");
        assertThat(paymentConsentDetails.getCharges()).isEqualTo(new FRAmount("0.25", "GBP"));
        assertThat(paymentConsentDetails.getExchangeRateInformation()).isEqualTo(consentEntity.getExchangeRateInformation());
        assertThat(paymentConsentDetails.getCurrencyOfTransfer()).isEqualTo("USD");
        assertThat(paymentConsentDetails.getPaymentDate()).isEqualTo(consentEntity.getRequestObj().getData().getInitiation().getRequestedExecutionDateTime());
        assertThat(paymentConsentDetails.getInstructedAmount()).isEqualTo(new FRAmount("10.01", "GBP"));
        assertThat(paymentConsentDetails.getInitiation()).isEqualTo(consentEntity.getRequestObj().getData().getInitiation());
        assertThat(paymentConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(paymentConsentDetails.getAccounts()).isEqualTo(testUserBankAccounts);
        assertThat(paymentConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(paymentConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(paymentConsentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(paymentConsentDetails.getDebtorAccount()).isNull();
    }

    @Test
    public void testGetInternationalPaymentDetailsWithDebtorAccount() throws ExceptionClient {
        final OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = new OBWriteDomestic2DataInitiationDebtorAccount().name("Test Account").identification("account-id-123").schemeName("accountId");
        mockApiClientServiceResponse();
        final FRAccountWithBalance accountWithBalance = FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance();
        mockAccountServiceGetByIdentifiersResponse(debtorAccount, accountWithBalance);
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final InternationalScheduledPaymentConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());

        consentEntity.getRequestObj().getData().getInitiation().setDebtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(debtorAccount));
        consentEntity.setId(intentId);
        given(internationalPaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(internationalPaymentConsentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, Mockito.mock(SignedJWT.class), testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(InternationalScheduledPaymentConsentDetails.class);
        InternationalScheduledPaymentConsentDetails paymentConsentDetails = (InternationalScheduledPaymentConsentDetails) consentDetails;
        assertThat(paymentConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(paymentConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(paymentConsentDetails.getPaymentReference()).isEqualTo("FRESCO-037");
        assertThat(paymentConsentDetails.getCharges()).isEqualTo(new FRAmount("0.25", "GBP"));
        assertThat(paymentConsentDetails.getExchangeRateInformation()).isEqualTo(consentEntity.getExchangeRateInformation());
        assertThat(paymentConsentDetails.getPaymentDate()).isEqualTo(consentEntity.getRequestObj().getData().getInitiation().getRequestedExecutionDateTime());
        assertThat(paymentConsentDetails.getInstructedAmount()).isEqualTo(new FRAmount("10.01", "GBP"));

        final FRWriteInternationalScheduledDataInitiation initiation = consentEntity.getRequestObj().getData().getInitiation();
        initiation.getDebtorAccount().setAccountId(accountWithBalance.getAccount().getAccountId());

        assertThat(paymentConsentDetails.getInitiation()).isEqualTo(initiation);
        assertThat(paymentConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(paymentConsentDetails.getAccounts()).isEqualTo(List.of(accountWithBalance));
        assertThat(paymentConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(paymentConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(paymentConsentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(paymentConsentDetails.getDebtorAccount()).isNotNull();
    }

}