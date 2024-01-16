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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.customerinfo;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.CustomerInfoConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.CustomerInfoService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.customerinfo.CustomerInfoConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.DefaultAccountAccessConsentServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo.CustomerInfoConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo.CustomerInfoConsentStateModel;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadConsent1;
import uk.org.openbanking.datamodel.account.OBReadData1;
import uk.org.openbanking.datamodel.account.OBRisk2;
import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;

import java.util.List;
import java.util.Optional;

import static com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.FRCustomerInfoTestHelper.aValidFRCustomerInfo;
import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo.DefaultCustomerInfoAccessConsentServiceTest.createValidConsentEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * Test for {@link CustomerInfoConsentDetailsService}
 */
@ExtendWith(MockitoExtension.class)
public class CustomerInfoConsentDetailsServiceTest {

    private static final String TEST_API_PROVIDER = "Test Api Provider";

    @Mock
    private CustomerInfoConsentService customerInfoConsentService;

    @Mock
    private CustomerInfoService customerInfoService;

    @Mock
    private ApiClientServiceClient apiClientServiceClient;

    @Mock
    private ApiProviderConfiguration apiProviderConfiguration;

    @InjectMocks
    private CustomerInfoConsentDetailsService consentDetailsService;

    private final User testUser;

    private final ApiClient testApiClient;

    private final Optional<FRCustomerInfo> optionalFRCustomerInfo;

    public CustomerInfoConsentDetailsServiceTest() {
        testUser = new User();
        testUser.setId("test-user-1");
        testUser.setUserName("testUser");

        testApiClient = new ApiClient();
        testApiClient.setId("acme-tpp-1");
        testApiClient.setName("ACME Corp");

        optionalFRCustomerInfo = Optional.ofNullable(aValidFRCustomerInfo(testUser.getId()));
    }

    @Test
    void shouldCreateCustomerInfoDetails() throws ExceptionClient {
        final String intentId = IntentType.CUSTOMER_INFO_CONSENT.generateIntentId();
        final CustomerInfoConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);
        shouldCreateCustomerInfoDetails(consentEntity);
    }

    @Test
    void shouldCreateCustomerInfoDetailsReAuthenticateConsent() throws ExceptionClient {
        final String intentId = IntentType.CUSTOMER_INFO_CONSENT.generateIntentId();
        final CustomerInfoConsentEntity consentEntity = createValidConsentEntity(testApiClient.getId());
        consentEntity.setId(intentId);
        consentEntity.setStatus(CustomerInfoConsentStateModel.getInstance().getAuthorisedConsentStatus());

        shouldCreateCustomerInfoDetails(consentEntity);
    }
    private void shouldCreateCustomerInfoDetails(CustomerInfoConsentEntity consentEntity) throws ExceptionClient {
        final String intentId = consentEntity.getId();
        given(apiClientServiceClient.getApiClient(eq(testApiClient.getId()))).willReturn(testApiClient);
        given(customerInfoService.getCustomerInformation(testUser.getId())).willReturn(optionalFRCustomerInfo);
        given(apiProviderConfiguration.getName()).willReturn(TEST_API_PROVIDER);
        given(customerInfoConsentService.getConsent(intentId, testApiClient.getId())).willReturn(consentEntity);
        given(customerInfoConsentService.canTransitionToAuthorisedState(eq(consentEntity))).willReturn(Boolean.TRUE);

        final ConsentDetails consentDetails = consentDetailsService.getDetailsFromConsentStore(
                new ConsentClientDetailsRequest(intentId, null, testUser, testApiClient.getId()));

        assertThat(consentDetails).isInstanceOf(CustomerInfoConsentDetails.class);
        CustomerInfoConsentDetails customerInfoConsentDetails = (CustomerInfoConsentDetails) consentDetails;
        assertThat(customerInfoConsentDetails.getConsentId()).isEqualTo(intentId);
        assertThat(customerInfoConsentDetails.getClientName()).isEqualTo(testApiClient.getName());
        assertThat(customerInfoConsentDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(customerInfoConsentDetails.getLogo()).isEqualTo(testApiClient.getLogoUri());
        assertThat(customerInfoConsentDetails.getServiceProviderName()).isEqualTo(TEST_API_PROVIDER);
        assertThat(customerInfoConsentDetails.getUserId()).isEqualTo(testUser.getId());
        assertThat(customerInfoConsentDetails.getCustomerInfo()).isEqualTo(optionalFRCustomerInfo.get());
        assertThat(customerInfoConsentDetails.getPermissions()).isEqualTo(List.of(FRExternalPermissionsCode.READCUSTOMERINFO));
    }
}
