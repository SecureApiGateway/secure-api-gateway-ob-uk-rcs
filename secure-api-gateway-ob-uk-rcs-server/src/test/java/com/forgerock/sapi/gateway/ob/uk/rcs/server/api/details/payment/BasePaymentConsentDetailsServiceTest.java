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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRChargeBearerType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;

import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationDebtorAccount;

public class BasePaymentConsentDetailsServiceTest {

    protected static final String TEST_API_PROVIDER = "Test Api Provider";
    protected final User testUser;
    protected final List<FRAccountWithBalance> testUserBankAccounts;
    protected final ApiClient testApiClient;
    @Mock
    protected AccountService accountService;
    @Mock
    protected ApiClientServiceClient apiClientServiceClient;
    @Mock
    protected ApiProviderConfiguration apiProviderConfiguration;

    public BasePaymentConsentDetailsServiceTest() {
        testUser = new User();
        testUser.setId("test-user-1");
        testUser.setUserName("testUser");

        testUserBankAccounts = List.of(FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance(),
                                       FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance());

        testApiClient = new ApiClient();
        testApiClient.setId("acme-tpp-1");
        testApiClient.setName("ACME Corp");
    }

    private static Stream<Arguments> chargeParameters() {
        return Stream.of(
                Arguments.of(List.of("0.01", "0.02", "0.2"), "0.23"),
                Arguments.of(List.of("0"), "0"),
                Arguments.of(List.of("0.25"), "0.25")
        );
    }

    @ParameterizedTest
    @MethodSource("chargeParameters")
    void testCalculateCharges(List<String> chargeAmounts, String expectedTotal) {
        final List<FRCharge> charges = chargeAmounts.stream().map(charge -> FRCharge.builder()
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .type("fee")
                        .amount(new FRAmount(charge, "GBP"))
                        .build())
                .collect(Collectors.toList());

        assertThat(BasePaymentConsentDetailsService.computeTotalChargeAmount(charges)).isEqualTo(new FRAmount(expectedTotal, "GBP"));
    }

    @Test
    void testCalculateChargesMismatchCcy() {
        final List<FRCharge> charges = List.of(
                FRCharge.builder()
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .type("fee")
                        .amount(new FRAmount("0.23", "EUR"))
                        .build(),
                FRCharge.builder()
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .type("fee")
                        .amount(new FRAmount("0.02", "GBP"))
                        .build()
        );

        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> BasePaymentConsentDetailsService.computeTotalChargeAmount(charges));
        assertThat(ex.getMessage()).isEqualTo("Charges contain more than 1 currency, all charges must be in the same currency");
    }

    protected void mockConsentServiceCanAuthorise(ConsentService<?, ?> consentService) {
        given(consentService.canTransitionToAuthorisedState(any())).willReturn(Boolean.TRUE);
    }

    protected void mockApiProviderConfigurationGetName() {
        given(apiProviderConfiguration.getName()).willReturn(TEST_API_PROVIDER);
    }

    protected void mockAccountServiceGetAccountsWithBalanceResponse() {
        given(accountService.getAccountsWithBalance(testUser.getId())).willReturn(testUserBankAccounts);
    }

    protected void mockApiClientServiceResponse() throws ExceptionClient {
        given(apiClientServiceClient.getApiClient(eq(testApiClient.getId()))).willReturn(testApiClient);
    }

    protected void mockAccountServiceGetByIdentifiersResponse(OBWriteDomestic2DataInitiationDebtorAccount debtorAccount, FRAccountWithBalance accountWithBalance) {
        given(accountService.getAccountWithBalanceByIdentifiers(eq(testUser.getId()), eq(debtorAccount.getName()),
                eq(debtorAccount.getIdentification()), eq(debtorAccount.getSchemeName()))).willReturn(accountWithBalance);
    }
}
