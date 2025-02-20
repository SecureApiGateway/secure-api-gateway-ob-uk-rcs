/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.domestic.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.v3_1_10.DefaultDomesticStandingOrderConsentServiceTest.createValidConsentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;

import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.domestic.DomesticStandingOrderConsentDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.common.FRAccountIdentifierConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticStandingOrderDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticStandingOrderConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticStandingOrderConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DomesticStandingOrderConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.nimbusds.jwt.SignedJWT;

import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2DataInitiationDebtorAccount;

@ExtendWith(MockitoExtension.class)
class DomesticStandingOrderConsentDetailsServiceTest extends BasePaymentConsentDetailsServiceTest {
    @Mock
    private DomesticStandingOrderConsentService consentService;

    @InjectMocks
    private DomesticStandingOrderConsentDetailsService consentDetailsService;


    @Test
    void testGetDomesticStandingOrderDetails() throws ExceptionClient {
        mockApiClientServiceResponse();
        mockAccountServiceGetAccountsWithBalanceResponse();
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final DomesticStandingOrderConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);
        given(consentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(consentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, null, testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(DomesticStandingOrderConsentDetails.class);
        DomesticStandingOrderConsentDetails domesticStandingOrderDetails = (DomesticStandingOrderConsentDetails) consentDetails;
        assertThat(domesticStandingOrderDetails.getConsentId()).isEqualTo(intentId);
        assertThat(domesticStandingOrderDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(domesticStandingOrderDetails.getPaymentReference()).isEqualTo("Ipsum Non Arcu Inc.");
        assertThat(domesticStandingOrderDetails.getCharges()).isEqualTo(new FRAmount("0.55", "GBP"));
        assertThat(domesticStandingOrderDetails.getInitiation()).isEqualTo(consentEntity.getRequestObj().getData().getInitiation());
        assertThat(domesticStandingOrderDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(domesticStandingOrderDetails.getAccounts()).isEqualTo(testUserBankAccounts);
        assertThat(domesticStandingOrderDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(domesticStandingOrderDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(domesticStandingOrderDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(domesticStandingOrderDetails.getDebtorAccount()).isNull();
    }

    @Test
    public void testGetDomesticStandingOrderDetailsWithDebtorAccount() throws ExceptionClient {
        final OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = new OBWriteDomestic2DataInitiationDebtorAccount().name("Test Account").identification("account-id-123").schemeName("accountId");
        mockApiClientServiceResponse();
        final FRAccountWithBalance accountWithBalance = FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance();
        given(accountService.getAccountWithBalanceByIdentifiers(eq(testUser.getId()),
                eq(debtorAccount.getIdentification()), eq(debtorAccount.getSchemeName()))).willReturn(accountWithBalance);
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final DomesticStandingOrderConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());

        consentEntity.getRequestObj().getData().getInitiation().setDebtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(debtorAccount));
        consentEntity.setId(intentId);
        given(consentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(consentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, Mockito.mock(SignedJWT.class), testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(DomesticStandingOrderConsentDetails.class);
        DomesticStandingOrderConsentDetails domesticScheduledConsentDetails = (DomesticStandingOrderConsentDetails) consentDetails;
        assertThat(domesticScheduledConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(domesticScheduledConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(domesticScheduledConsentDetails.getPaymentReference()).isEqualTo("Ipsum Non Arcu Inc.");
        assertThat(domesticScheduledConsentDetails.getCharges()).isEqualTo(new FRAmount("0.55", "GBP"));

        final FRWriteDomesticStandingOrderDataInitiation initiation = consentEntity.getRequestObj().getData().getInitiation();
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