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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.funds.FRFundsConfirmationConsentData;
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
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.FundsConfirmationConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.openbanking.datamodel.common.OBExternalAccountIdentification2Code;

import java.util.UUID;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.DefaultFundsConfirmationAccessConsentServiceTest.createValidConsentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

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

    private final FRAccountIdentifier accountIdentifier;

    public FundsConfirmationConsentDetailsServiceTest() {
        testUser = new User();
        testUser.setId("test-user-1");
        testUser.setUserName("testUser");

        testApiClient = new ApiClient();
        testApiClient.setId("acme-tpp-1");
        testApiClient.setName("ACME Corp");

        accountIdentifier = FRAccountIdentifier.builder()
                .accountId(UUID.randomUUID().toString())
                .name("account-name")
                .schemeName(OBExternalAccountIdentification2Code.SortCodeAccountNumber.toString())
                .identification("08080021325698")
                .secondaryIdentification("secondary-identification")
                .build();
    }

    @Test
    void shouldCreateFundsConfirmationConsentDetails() throws ExceptionClient {
        final String intentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        final FundsConfirmationConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId(), accountIdentifier);
        consentEntity.setId(intentId);
        testCreateFundsConfirmationConsentDetails(consentEntity);
    }

    private void testCreateFundsConfirmationConsentDetails(FundsConfirmationConsentEntity consentEntity) throws ExceptionClient {
        final String intentId = consentEntity.getId();
        given(apiClientServiceClient.getApiClient(eq(testApiClient.getId()))).willReturn(testApiClient);
        given(apiProviderConfiguration.getName()).willReturn(TEST_API_PROVIDER);
        given(accountService.getAccountIdentifier(
                testUser.getId(), accountIdentifier.getName(), accountIdentifier.getIdentification(), accountIdentifier.getSchemeName())
        ).willReturn(accountIdentifier);
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

        final FRFundsConfirmationConsentData consentData = consentEntity.getRequestObj().getData();
        assertThat(fundsConfirmationConsentDetails.getExpirationDateTime()).isEqualTo(consentData.getExpirationDateTime());
        assertThat(fundsConfirmationConsentDetails.getDebtorAccount()).isEqualTo(consentData.getDebtorAccount());
    }
}
