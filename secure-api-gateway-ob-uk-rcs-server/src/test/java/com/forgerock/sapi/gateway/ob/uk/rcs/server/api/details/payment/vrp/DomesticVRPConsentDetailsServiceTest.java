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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.vrp;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.vrp.DefaultDomesticVRPConsentServiceTest.createValidConsentEntity;
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
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRAccountIdentifierConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRWriteDomesticVrpDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticVrpPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.vrp.DomesticVRPConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.vrp.DomesticVRPConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.nimbusds.jwt.SignedJWT;

import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationDebtorAccount;

@ExtendWith(MockitoExtension.class)
class DomesticVRPConsentDetailsServiceTest extends BasePaymentConsentDetailsServiceTest {

    @Mock
    private DomesticVRPConsentService consentService;

    @InjectMocks
    private DomesticVRPConsentDetailsService consentDetailsService;

    @Test
    void testGetDomesticVRPDetails() throws ExceptionClient {
        mockApiClientServiceResponse();
        mockAccountServiceGetAccountsWithBalanceResponse();
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final DomesticVRPConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);
        given(consentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(consentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, null, testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(DomesticVrpPaymentConsentDetails.class);
        DomesticVrpPaymentConsentDetails vrpConsentDetails = (DomesticVrpPaymentConsentDetails) consentDetails;
        assertThat(vrpConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(vrpConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(vrpConsentDetails.getInitiation()).isEqualTo(consentEntity.getRequestObj().getData().getInitiation());
        assertThat(vrpConsentDetails.getControlParameters()).isEqualTo(consentEntity.getRequestObj().getData().getControlParameters());
        assertThat(vrpConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(vrpConsentDetails.getAccounts()).isEqualTo(testUserBankAccounts);
        assertThat(vrpConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(vrpConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(vrpConsentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(vrpConsentDetails.getDebtorAccount()).isNull();
    }

    @Test
    public void testGetDomesticVRPDetailsWithDebtorAccount() throws ExceptionClient {
        final OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = new OBWriteDomestic2DataInitiationDebtorAccount().name("Test Account").identification("account-id-123").schemeName("accountId");
        mockApiClientServiceResponse();
        final FRAccountWithBalance accountWithBalance = FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance();
        mockAccountServiceGetByIdentifiersResponse(debtorAccount, accountWithBalance);
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final DomesticVRPConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());

        consentEntity.getRequestObj().getData().getInitiation().setDebtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(debtorAccount));
        consentEntity.setId(intentId);
        given(consentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(consentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, Mockito.mock(SignedJWT.class), testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(DomesticVrpPaymentConsentDetails.class);
        DomesticVrpPaymentConsentDetails vrpConsentDetails = (DomesticVrpPaymentConsentDetails) consentDetails;
        assertThat(vrpConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(vrpConsentDetails.getClientName()).isEqualTo(testApiClient.getName());

        final FRWriteDomesticVrpDataInitiation initiation = consentEntity.getRequestObj().getData().getInitiation();
        initiation.getDebtorAccount().setAccountId(accountWithBalance.getAccount().getAccountId());

        assertThat(vrpConsentDetails.getInitiation()).isEqualTo(initiation);
        assertThat(vrpConsentDetails.getControlParameters()).isEqualTo(consentEntity.getRequestObj().getData().getControlParameters());
        assertThat(vrpConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(vrpConsentDetails.getAccounts()).isEqualTo(List.of(accountWithBalance));
        assertThat(vrpConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(vrpConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(vrpConsentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(vrpConsentDetails.getDebtorAccount()).isNotNull();
    }

}