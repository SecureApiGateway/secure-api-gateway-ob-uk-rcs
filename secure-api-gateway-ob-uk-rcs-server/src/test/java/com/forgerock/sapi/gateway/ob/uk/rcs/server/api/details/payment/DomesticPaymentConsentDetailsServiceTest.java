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

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DefaultDomesticPaymentConsentServiceTest.createValidConsentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.DebtorAccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5DataCharges;

@ExtendWith(MockitoExtension.class)
class DomesticPaymentConsentDetailsServiceTest {

    private static final String TEST_API_PROVIDER = "Test Api Provider";

    @Mock
    private DomesticPaymentConsentService domesticPaymentConsentService;

    @Mock
    private AccountService accountService;

    @Mock
    private ApiClientServiceClient apiClientServiceClient;

    @Mock
    private ApiProviderConfiguration apiProviderConfiguration;

    @Mock
    private DebtorAccountService debtorAccountService;

    @InjectMocks
    private DomesticPaymentConsentDetailsService consentDetailsService;

    private final User testUser;

    private final List<FRAccountWithBalance> testUserBankAccounts;

    private final ApiClient testApiClient;

    public DomesticPaymentConsentDetailsServiceTest() {
        testUser = new User();
        testUser.setId("test-user-1");
        testUser.setUserName("testUser");

        testApiClient = new ApiClient();
        testApiClient.setId("acme-tpp-1");
        testApiClient.setName("ACME Corp");

        testUserBankAccounts = List.of(FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance());
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
        final List<OBWriteDomesticConsentResponse5DataCharges> charges = chargeAmounts.stream().map(charge -> {
            final OBWriteDomesticConsentResponse5DataCharges obCharge = new OBWriteDomesticConsentResponse5DataCharges();
            obCharge.setAmount(new OBActiveOrHistoricCurrencyAndAmount().amount(charge).currency("GBP"));
            return obCharge;
        }).collect(Collectors.toList());

        assertThat(DomesticPaymentConsentDetailsService.computeTotalChargeAmount(charges)).isEqualTo(new FRAmount(expectedTotal, "GBP"));
    }

    @Test
    void testCalculateChargesMismatchCcy() {
        final List<OBWriteDomesticConsentResponse5DataCharges> charges = List.of(
                new OBWriteDomesticConsentResponse5DataCharges().amount(new OBActiveOrHistoricCurrencyAndAmount().amount("0.01").currency("GBP")),
                new OBWriteDomesticConsentResponse5DataCharges().amount(new OBActiveOrHistoricCurrencyAndAmount().amount("0.2").currency("EUR"))
        );

        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> DomesticPaymentConsentDetailsService.computeTotalChargeAmount(charges));
        assertThat(ex.getMessage()).isEqualTo("Charges contain more than 1 currency, all charges must be in the same currency");
    }


    @Test
    void testGetDomesticPaymentDetails() throws ExceptionClient {
        given(apiClientServiceClient.getApiClient(eq(testApiClient.getId()))).willReturn(testApiClient);
        given(accountService.getAccountsWithBalance(testUser.getId())).willReturn(testUserBankAccounts);
        given(apiProviderConfiguration.getName()).willReturn(TEST_API_PROVIDER);

        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final DomesticPaymentConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);
        given(domesticPaymentConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, null, testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(DomesticPaymentConsentDetails.class);
        DomesticPaymentConsentDetails domesticPaymentConsentDetails = (DomesticPaymentConsentDetails) consentDetails;
        assertThat(domesticPaymentConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(domesticPaymentConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(domesticPaymentConsentDetails.getPaymentReference()).isEqualTo("FRESCO-037");
        assertThat(domesticPaymentConsentDetails.getCharges()).isEqualTo(new FRAmount("0.25", "GBP"));
        assertThat(domesticPaymentConsentDetails.getInstructedAmount()).isEqualTo(new FRAmount("10.01", "GBP"));
        assertThat(domesticPaymentConsentDetails.getInitiation()).isEqualTo(FRWriteDomesticConsentConverter.toFRWriteDomesticDataInitiation(consentEntity.getRequestObj().getData().getInitiation()));
        assertThat(domesticPaymentConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(domesticPaymentConsentDetails.getAccounts()).isEqualTo(testUserBankAccounts);
        assertThat(domesticPaymentConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(domesticPaymentConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(domesticPaymentConsentDetails.getUserId()).isEqualTo(testUser.getId());

        assertThat(domesticPaymentConsentDetails.getDebtorAccount()).isNull();

        verifyNoInteractions(debtorAccountService);
    }

}