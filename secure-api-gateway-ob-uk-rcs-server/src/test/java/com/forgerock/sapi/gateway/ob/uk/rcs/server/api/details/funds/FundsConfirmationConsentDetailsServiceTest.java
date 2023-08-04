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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.funds;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.DefaultFundsConfirmationAccessConsentServiceTest.createValidConsentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.funds.FRFundsConfirmationConsentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.FundsConfirmationConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.funds.FundsConfirmationConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.FundsConfirmationConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.FundsConfirmationConsentStateModel;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

/**
 * Test for {@link FundsConfirmationConsentDetailsService}
 */
@ExtendWith(MockitoExtension.class)
public class FundsConfirmationConsentDetailsServiceTest {

    private static final String TEST_API_PROVIDER = "Test Api Provider";

    @Mock
    private AccountService accountService;

    @Mock
    private FundsConfirmationConsentService fundsConfirmationConsentService;

    @Mock
    private ApiClientServiceClient apiClientServiceClient;

    @Mock
    private ApiProviderConfiguration apiProviderConfiguration;

    @InjectMocks
    private FundsConfirmationConsentDetailsService consentDetailsService;

    private final User testUser;

    private final ApiClient testApiClient;

    private final FRAccountWithBalance accountWithBalance;
    private final FRAccountIdentifier accountIdentifier;

    public FundsConfirmationConsentDetailsServiceTest() {
        testUser = new User();
        testUser.setId("test-user-1");
        testUser.setUserName("testUser");

        testApiClient = new ApiClient();
        testApiClient.setId("acme-tpp-1");
        testApiClient.setName("ACME Corp");

        accountWithBalance = FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance();
        accountIdentifier = accountWithBalance.getAccount().getFirstAccount();
    }

    @Test
    void shouldCreateFundsConfirmationConsentDetails() throws ExceptionClient {
        final String intentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        final FundsConfirmationConsentEntity consentEntity = createValidConsentEntity(
                testApiClient.getId(),
                accountIdentifier
        );
        consentEntity.setId(intentId);
        mockAccountService(testUser.getId(), accountWithBalance);
        testCreateFundsConfirmationConsentDetails(consentEntity);
    }

    @Test
    void shouldCreateFundsConfirmationConsentReauthenticateConsent() throws ExceptionClient {
        final String intentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        final FundsConfirmationConsentEntity consentEntity = createValidConsentEntity(
                testApiClient.getId(),
                accountIdentifier
        );
        consentEntity.setId(intentId);
        consentEntity.setStatus(FundsConfirmationConsentStateModel.getInstance().getAuthorisedConsentStatus());
        mockAccountService(testUser.getId(), accountWithBalance);
        testCreateFundsConfirmationConsentDetails(consentEntity);
    }

    @Test
    void shouldThrowsDebtorAccountNotFound() {
        final String intentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        final FundsConfirmationConsentEntity consentEntity = createValidConsentEntity(
                testApiClient.getId(),
                accountIdentifier
        );
        consentEntity.setId(intentId);
        consentEntity.setStatus(FundsConfirmationConsentStateModel.getInstance().getAuthorisedConsentStatus());
        mockAccountService(testUser.getId(), null);
        ConsentStoreException exception = assertThrows(ConsentStoreException.class, () -> testCreateFundsConfirmationConsentDetails(consentEntity));

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorType()).isEqualTo(ConsentStoreException.ErrorType.NOT_FOUND);
        assertThat(exception.getMessage()).contains("DebtorAccount not found for user");
    }

    private void testCreateFundsConfirmationConsentDetails(FundsConfirmationConsentEntity consentEntity) throws ExceptionClient {
        final String intentId = consentEntity.getId();
        given(apiClientServiceClient.getApiClient(eq(testApiClient.getId()))).willReturn(testApiClient);
        given(apiProviderConfiguration.getName()).willReturn(TEST_API_PROVIDER);

        given(fundsConfirmationConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        given(fundsConfirmationConsentService.canTransitionToAuthorisedState(eq(consentEntity))).willReturn(Boolean.TRUE);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, null, testUser, testApiClient.getId())
        );

        assertThat(consentDetails).isInstanceOf(FundsConfirmationConsentDetails.class);
        FundsConfirmationConsentDetails fundsConfirmationConsentDetails = (FundsConfirmationConsentDetails) consentDetails;
        assertThat(fundsConfirmationConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(fundsConfirmationConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(fundsConfirmationConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(fundsConfirmationConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(fundsConfirmationConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(fundsConfirmationConsentDetails.getUserId()).isEqualTo(testUser.getId());
        assertThat(fundsConfirmationConsentDetails.getAccounts()).isNotNull().isNotEmpty();

        final FRFundsConfirmationConsentData consentData = consentEntity.getRequestObj().getData();
        assertThat(fundsConfirmationConsentDetails.getExpirationDateTime()).isEqualTo(consentData.getExpirationDateTime());
        assertThat(fundsConfirmationConsentDetails.getDebtorAccount()).isEqualTo(consentData.getDebtorAccount());
    }

    private void mockAccountService(String userId, FRAccountWithBalance willReturn) {
        given(accountService.getAccountWithBalanceByIdentifiers(
                        eq(userId), eq(accountIdentifier.getName()),
                        eq(accountIdentifier.getIdentification()), eq(accountIdentifier.getSchemeName())
                )
        ).willReturn(willReturn);
    }
}
