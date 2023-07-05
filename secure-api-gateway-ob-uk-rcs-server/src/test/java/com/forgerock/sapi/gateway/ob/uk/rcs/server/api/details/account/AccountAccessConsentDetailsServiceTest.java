/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.account;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.DefaultAccountAccessConsentServiceTest.createValidConsentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRReadConsentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@ExtendWith(MockitoExtension.class)
class AccountAccessConsentDetailsServiceTest {

    private static final String TEST_API_PROVIDER = "Test Api Provider";

    @Mock
    private AccountAccessConsentService accountAccessConsentService;

    @Mock
    private AccountService accountService;

    @Mock
    private ApiClientServiceClient apiClientServiceClient;

    @Mock
    private ApiProviderConfiguration apiProviderConfiguration;

    @InjectMocks
    private AccountAccessConsentDetailsService consentDetailsService;

    private final User testUser;

    private final List<FRAccountWithBalance> testUserBankAccounts;

    private final ApiClient testApiClient;

    public AccountAccessConsentDetailsServiceTest() {
        testUser = new User();
        testUser.setId("test-user-1");
        testUser.setUserName("testUser");

        testApiClient = new ApiClient();
        testApiClient.setId("acme-tpp-1");
        testApiClient.setName("ACME Corp");

        testUserBankAccounts = List.of(FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance(), FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance());
    }

    @Test
    void testCreateAccountAccessDetails() throws ExceptionClient {
        final String intentId = IntentType.ACCOUNT_ACCESS_CONSENT.generateIntentId();
        final AccountAccessConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);

        testCreateAccountAccessDetails(consentEntity);
    }

    @Test
    void testCreateAccountAccessDetailsReAuthenticateConsent() throws ExceptionClient {
        final String intentId = IntentType.ACCOUNT_ACCESS_CONSENT.generateIntentId();
        final AccountAccessConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);
        consentEntity.setStatus(AccountAccessConsentStateModel.getInstance().getAuthorisedConsentStatus());

        testCreateAccountAccessDetails(consentEntity);
    }

    private void testCreateAccountAccessDetails(AccountAccessConsentEntity consentEntity) throws ExceptionClient {
        final String intentId = consentEntity.getId();
        given(apiClientServiceClient.getApiClient(eq(testApiClient.getId()))).willReturn(testApiClient);
        given(accountService.getAccountsWithBalance(testUser.getId())).willReturn(testUserBankAccounts);
        given(apiProviderConfiguration.getName()).willReturn(TEST_API_PROVIDER);
        given(accountAccessConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        given(accountAccessConsentService.canTransitionToAuthorisedState(eq(consentEntity))).willReturn(Boolean.TRUE);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, null, testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(AccountsConsentDetails.class);
        AccountsConsentDetails accountsConsentDetails = (AccountsConsentDetails) consentDetails;
        assertThat(accountsConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(accountsConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(accountsConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(accountsConsentDetails.getAccounts()).isEqualTo(testUserBankAccounts);
        assertThat(accountsConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(accountsConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(accountsConsentDetails.getUserId()).isEqualTo(testUser.getId());

        final FRReadConsentData consentData = consentEntity.getRequestObj().getData();
        assertThat(accountsConsentDetails.getExpiredDate()).isEqualTo(consentData.getExpirationDateTime());
        assertThat(accountsConsentDetails.getFromTransaction()).isEqualTo(consentData.getTransactionFromDateTime());
        assertThat(accountsConsentDetails.getToTransaction()).isEqualTo(consentData.getTransactionToDateTime());
        assertThat(accountsConsentDetails.getPermissions()).isEqualTo(List.of(FRExternalPermissionsCode.READACCOUNTSBASIC));
    }

}