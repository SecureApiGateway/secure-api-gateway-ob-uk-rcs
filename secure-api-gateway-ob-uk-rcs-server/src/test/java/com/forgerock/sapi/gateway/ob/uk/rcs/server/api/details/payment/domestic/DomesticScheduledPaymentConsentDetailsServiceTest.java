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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.domestic;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DefaultDomesticScheduledPaymentConsentServiceTest.createValidConsentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticScheduledDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticScheduledPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticScheduledPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DomesticScheduledPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.nimbusds.jwt.SignedJWT;

import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2DataInitiationDebtorAccount;

@ExtendWith(MockitoExtension.class)
class DomesticScheduledPaymentConsentDetailsServiceTest extends BasePaymentConsentDetailsServiceTest {
    @Mock
    private DomesticScheduledPaymentConsentService domesticScheduledPaymentConsentService;

    @InjectMocks
    private DomesticScheduledPaymentConsentDetailsService consentDetailsService;


    @Test
    void testGetDomesticScheduledPaymentDetails() throws ExceptionClient {
        mockApiClientServiceResponse();
        mockAccountServiceGetAccountsWithBalanceResponse();
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final DomesticScheduledPaymentConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);
        given(domesticScheduledPaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(domesticScheduledPaymentConsentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, null, testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(DomesticScheduledPaymentConsentDetails.class);
        DomesticScheduledPaymentConsentDetails domesticScheduledPaymentDetails = (DomesticScheduledPaymentConsentDetails) consentDetails;
        assertThat(domesticScheduledPaymentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(domesticScheduledPaymentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(domesticScheduledPaymentDetails.getPaymentReference()).isEqualTo("FRESCO-037");
        assertThat(domesticScheduledPaymentDetails.getCharges()).isEqualTo(new FRAmount("0.25", "GBP"));
        assertThat(domesticScheduledPaymentDetails.getPaymentDate()).isEqualTo(consentEntity.getRequestObj().getData().getInitiation().getRequestedExecutionDateTime());
        assertThat(domesticScheduledPaymentDetails.getInstructedAmount()).isEqualTo(new FRAmount("10.01", "GBP"));
        assertThat(domesticScheduledPaymentDetails.getInitiation()).isEqualTo(consentEntity.getRequestObj().getData().getInitiation());
        assertThat(domesticScheduledPaymentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(domesticScheduledPaymentDetails.getAccounts()).isEqualTo(testUserBankAccounts);
        assertThat(domesticScheduledPaymentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(domesticScheduledPaymentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(domesticScheduledPaymentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(domesticScheduledPaymentDetails.getDebtorAccount()).isNull();
    }

    @Test
    public void testGetDomesticScheduledPaymentDetailsWithDebtorAccount() throws ExceptionClient {
        final OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = new OBWriteDomestic2DataInitiationDebtorAccount().name("Test Account").identification("account-id-123").schemeName("accountId");
        mockApiClientServiceResponse();
        final FRAccountWithBalance accountWithBalance = FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance();
        given(accountService.getAccountWithBalanceByIdentifiers(eq(testUser.getId()),
                eq(debtorAccount.getIdentification()), eq(debtorAccount.getSchemeName()))).willReturn(accountWithBalance);
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final DomesticScheduledPaymentConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());

        consentEntity.getRequestObj().getData().getInitiation().setDebtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(debtorAccount));
        consentEntity.setId(intentId);
        given(domesticScheduledPaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(domesticScheduledPaymentConsentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, Mockito.mock(SignedJWT.class), testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(DomesticScheduledPaymentConsentDetails.class);
        DomesticScheduledPaymentConsentDetails domesticScheduledConsentDetails = (DomesticScheduledPaymentConsentDetails) consentDetails;
        assertThat(domesticScheduledConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(domesticScheduledConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(domesticScheduledConsentDetails.getPaymentReference()).isEqualTo("FRESCO-037");
        assertThat(domesticScheduledConsentDetails.getCharges()).isEqualTo(new FRAmount("0.25", "GBP"));
        assertThat(domesticScheduledConsentDetails.getPaymentDate()).isEqualTo(consentEntity.getRequestObj().getData().getInitiation().getRequestedExecutionDateTime());
        assertThat(domesticScheduledConsentDetails.getInstructedAmount()).isEqualTo(new FRAmount("10.01", "GBP"));

        final FRWriteDomesticScheduledDataInitiation initiation = consentEntity.getRequestObj().getData().getInitiation();
        initiation.getDebtorAccount().setAccountId(accountWithBalance.getAccount().getAccountId());

        assertThat(domesticScheduledConsentDetails.getInitiation()).isEqualTo(initiation);
        assertThat(domesticScheduledConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(domesticScheduledConsentDetails.getAccounts()).isEqualTo(List.of(accountWithBalance));
        assertThat(domesticScheduledConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(domesticScheduledConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(domesticScheduledConsentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(domesticScheduledConsentDetails.getDebtorAccount()).isNotNull();
    }

}