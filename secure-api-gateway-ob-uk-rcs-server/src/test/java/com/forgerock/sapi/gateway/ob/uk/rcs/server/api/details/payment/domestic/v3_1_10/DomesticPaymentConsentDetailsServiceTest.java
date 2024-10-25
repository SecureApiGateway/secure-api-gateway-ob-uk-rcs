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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.domestic.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.v3_1_10.DefaultDomesticPaymentConsentServiceTest.createValidConsentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;

import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.domestic.DomesticPaymentConsentDetailsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.common.FRAccountIdentifierConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.nimbusds.jwt.SignedJWT;

import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2DataInitiationDebtorAccount;

@ExtendWith(MockitoExtension.class)
class DomesticPaymentConsentDetailsServiceTest extends BasePaymentConsentDetailsServiceTest {

    @Mock
    private DomesticPaymentConsentService domesticPaymentConsentService;

    @InjectMocks
    private DomesticPaymentConsentDetailsService consentDetailsService;

    @Test
    void testGetDomesticPaymentDetails() throws ExceptionClient {
        mockApiClientServiceResponse();
        mockAccountServiceGetAccountsWithBalanceResponse();
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final DomesticPaymentConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);
        given(domesticPaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(domesticPaymentConsentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, null, testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(DomesticPaymentConsentDetails.class);
        DomesticPaymentConsentDetails domesticPaymentConsentDetails = (DomesticPaymentConsentDetails) consentDetails;
        assertThat(domesticPaymentConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(domesticPaymentConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(domesticPaymentConsentDetails.getPaymentReference()).isEqualTo("FRESCO-037");
        assertThat(domesticPaymentConsentDetails.getCharges()).isEqualTo(new FRAmount("0.25", "GBP"));
        assertThat(domesticPaymentConsentDetails.getInstructedAmount()).isEqualTo(new FRAmount("10.01", "GBP"));
        assertThat(domesticPaymentConsentDetails.getInitiation()).isEqualTo(consentEntity.getRequestObj().getData().getInitiation());
        assertThat(domesticPaymentConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(domesticPaymentConsentDetails.getAccounts()).isEqualTo(testUserBankAccounts);
        assertThat(domesticPaymentConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(domesticPaymentConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(domesticPaymentConsentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(domesticPaymentConsentDetails.getDebtorAccount()).isNull();
    }

    @Test
    public void testGetDomesticPaymentDetailsWithDebtorAccount() throws ExceptionClient {
        final OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = new OBWriteDomestic2DataInitiationDebtorAccount().name("Test Account").identification("account-id-123").schemeName("accountId");
        mockApiClientServiceResponse();
        final FRAccountWithBalance accountWithBalance = FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance();
        mockAccountServiceGetByIdentifiersResponse(debtorAccount, accountWithBalance);
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final DomesticPaymentConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());

        consentEntity.getRequestObj().getData().getInitiation().setDebtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(debtorAccount));
        consentEntity.setId(intentId);
        given(domesticPaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(domesticPaymentConsentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, Mockito.mock(SignedJWT.class), testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(DomesticPaymentConsentDetails.class);
        DomesticPaymentConsentDetails domesticPaymentConsentDetails = (DomesticPaymentConsentDetails) consentDetails;
        assertThat(domesticPaymentConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(domesticPaymentConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(domesticPaymentConsentDetails.getPaymentReference()).isEqualTo("FRESCO-037");
        assertThat(domesticPaymentConsentDetails.getCharges()).isEqualTo(new FRAmount("0.25", "GBP"));
        assertThat(domesticPaymentConsentDetails.getInstructedAmount()).isEqualTo(new FRAmount("10.01", "GBP"));

        final FRWriteDomesticDataInitiation initiation = consentEntity.getRequestObj().getData().getInitiation();
        initiation.getDebtorAccount().setAccountId(accountWithBalance.getAccount().getAccountId());

        assertThat(domesticPaymentConsentDetails.getInitiation()).isEqualTo(initiation);
        assertThat(domesticPaymentConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(domesticPaymentConsentDetails.getAccounts()).isEqualTo(List.of(accountWithBalance));
        assertThat(domesticPaymentConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(domesticPaymentConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(domesticPaymentConsentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(domesticPaymentConsentDetails.getDebtorAccount()).isNotNull();
    }

    @Test
    void testConsentReAuthenticationNotSupported() {
        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        given(domesticPaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(createValidConsentEntity(testApiClient.getId()));

        given(domesticPaymentConsentService.canTransitionToAuthorisedState(any())).willReturn(Boolean.FALSE);

        final ConsentStoreException consentStoreException = Assertions.assertThrows(ConsentStoreException.class,
                () -> consentDetailsService.getDetailsFromConsentStore(new ConsentClientDetailsRequest(intentId, Mockito.mock(SignedJWT.class), testUser, testApiClient.getId())));
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.CONSENT_REAUTHENTICATION_NOT_SUPPORTED);
    }

    @Test
    public void failToGetDomesticPaymentDetailsWithDebtorAccountWhenAccountNotOwnedByUser() throws ExceptionClient {
        final OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = new OBWriteDomestic2DataInitiationDebtorAccount().name("Test Account").identification("account-id-123").schemeName("accountId");
        mockApiClientServiceResponse();
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final DomesticPaymentConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());

        consentEntity.getRequestObj().getData().getInitiation().setDebtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(debtorAccount));
        consentEntity.setId(intentId);
        given(domesticPaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(domesticPaymentConsentService);

        final ConsentStoreException consentStoreException = Assertions.assertThrows(ConsentStoreException.class,
                () -> consentDetailsService.getDetailsFromConsentStore(new ConsentClientDetailsRequest(intentId, Mockito.mock(SignedJWT.class), testUser, testApiClient.getId())));
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.INVALID_DEBTOR_ACCOUNT);
        assertThat(consentStoreException.getMessage()).contains("DebtorAccount not found for user");
    }


}