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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.file;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.file.DefaultFilePaymentConsentServiceTest.createValidConsentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
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
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteFileDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.FilePaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.file.FilePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.file.FilePaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.nimbusds.jwt.SignedJWT;

import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationDebtorAccount;


@ExtendWith(MockitoExtension.class)
class FilePaymentConsentDetailsServiceTest extends BasePaymentConsentDetailsServiceTest {

    @Mock
    private FilePaymentConsentService filePaymentConsentService;

    @InjectMocks
    private FilePaymentConsentDetailsService consentDetailsService;

    @Test
    void testGetFilePaymentDetails() throws ExceptionClient {
        mockApiClientServiceResponse();
        mockAccountServiceGetAccountsWithBalanceResponse();
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();

        final FilePaymentConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);
        given(filePaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(filePaymentConsentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, null, testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(FilePaymentConsentDetails.class);
        FilePaymentConsentDetails filePaymentConsentDetails = (FilePaymentConsentDetails) consentDetails;
        assertThat(filePaymentConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(filePaymentConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(filePaymentConsentDetails.getPaymentReference()).isEqualTo("FRESCO-037");
        assertThat(filePaymentConsentDetails.getCharges()).isEqualTo(new FRAmount("0.25", "GBP"));
        assertThat(filePaymentConsentDetails.getFilePayment()).isEqualTo(consentEntity.getRequestObj().getData().getInitiation());
        assertThat(filePaymentConsentDetails.getFileReference()).isEqualTo("GB2OK238");
        assertThat(filePaymentConsentDetails.getControlSum()).isEqualTo(BigDecimal.ONE.setScale(4));
        assertThat(filePaymentConsentDetails.getNumberOfTransactions()).isEqualTo("3");
        assertThat(filePaymentConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(filePaymentConsentDetails.getAccounts()).isEqualTo(testUserBankAccounts);
        assertThat(filePaymentConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(filePaymentConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(filePaymentConsentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(filePaymentConsentDetails.getDebtorAccount()).isNull();
    }

    @Test
    public void testGetFilePaymentDetailsWithDebtorAccount() throws ExceptionClient {
        final OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = new OBWriteDomestic2DataInitiationDebtorAccount().name("Test Account").identification("account-id-123").schemeName("accountId");
        mockApiClientServiceResponse();
        final FRAccountWithBalance accountWithBalance = FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance();
        mockAccountServiceGetByIdentifiersResponse(debtorAccount, accountWithBalance);
        mockApiProviderConfigurationGetName();

        final String intentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();

        final FilePaymentConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());

        consentEntity.getRequestObj().getData().getInitiation().setDebtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(debtorAccount));
        consentEntity.setId(intentId);
        given(filePaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        mockConsentServiceCanAuthorise(filePaymentConsentService);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, Mockito.mock(SignedJWT.class), testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(FilePaymentConsentDetails.class);
        FilePaymentConsentDetails filePaymentConsentDetails = (FilePaymentConsentDetails) consentDetails;
        assertThat(filePaymentConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(filePaymentConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(filePaymentConsentDetails.getPaymentReference()).isEqualTo("FRESCO-037");
        assertThat(filePaymentConsentDetails.getCharges()).isEqualTo(new FRAmount("0.25", "GBP"));

        final FRWriteFileDataInitiation initiation = consentEntity.getRequestObj().getData().getInitiation();
        initiation.getDebtorAccount().setAccountId(accountWithBalance.getAccount().getAccountId());

        assertThat(filePaymentConsentDetails.getFilePayment()).isEqualTo(initiation);
        assertThat(filePaymentConsentDetails.getFileReference()).isEqualTo("GB2OK238");
        assertThat(filePaymentConsentDetails.getControlSum()).isEqualTo(BigDecimal.ONE.setScale(4));
        assertThat(filePaymentConsentDetails.getNumberOfTransactions()).isEqualTo("3");
        assertThat(filePaymentConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(filePaymentConsentDetails.getAccounts()).isEqualTo(List.of(accountWithBalance));
        assertThat(filePaymentConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(filePaymentConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(filePaymentConsentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(filePaymentConsentDetails.getDebtorAccount()).isNotNull();
    }

}