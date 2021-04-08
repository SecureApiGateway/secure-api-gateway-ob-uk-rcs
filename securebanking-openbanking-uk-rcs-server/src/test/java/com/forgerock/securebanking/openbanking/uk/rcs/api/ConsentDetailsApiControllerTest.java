/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.api;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.PaymentConsentDecision;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRDomesticPaymentConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRFilePaymentConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRFundsConfirmationConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRInternationalPaymentConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.testsupport.TestInitializer;
import com.forgerock.securebanking.openbanking.uk.rcs.testsupport.WireMockServerExtension;
import com.forgerock.securebanking.openbanking.uk.rcs.testsupport.WireMockStubHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.tpp.TppTestDataFactory.aValidTppBuilder;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.JwtHelper.consentRequestJwt;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRDomesticPaymentConsentDataTestDataFactory.aValidDomesticPaymentConsentDataBuilder;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRDomesticPaymentConsentTestDataFactory.aValidFRDomesticPaymentConsentBuilder;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRFilePaymentConsentDataTestDataFactory.aValidFilePaymentConsentDataBuilder;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRFilePaymentConsentTestDataFactory.aValidFRFilePaymentConsentBuilder;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRFundsConfirmationConsentDataTestDataFactory.aValidFundsConfirmationConsentDataBuilder;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRFundsConfirmationConsentTestDataFactory.aValidFRFundsConfirmationConsentBuilder;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRInternationalPaymentConsentDataTestDataFactory.aValidInternationalPaymentConsentDataBuilder;
import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRInternationalPaymentConsentTestDataFactory.aValidFRInternationalPaymentConsentBuilder;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.org.openbanking.testsupport.payment.OBAccountTestDataFactory.aValidOBCashAccount3;
import static uk.org.openbanking.testsupport.payment.OBAccountTestDataFactory.aValidOBWriteDomestic2DataInitiationDebtorAccount;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomestic2DataInitiation;
import static uk.org.openbanking.testsupport.payment.OBWriteFileConsentTestDataFactory.aValidOBWriteFile2DataInitiation;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternational3DataInitiation;

/**
 * Spring Boot Test for {@link ConsentDetailsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = TestInitializer.class)
@ActiveProfiles("test")
@ExtendWith(WireMockServerExtension.class)
public class ConsentDetailsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String DETAILS_URI = "/api/rcs/consent/details";
    private static final String CLIENT_ID = "a12f9ebc-4966-4543-afe0-03e597835a01";
    private static final String USER_ID = "45c6486e-8fc0-3ffc-h6f5-2105164d01j4";
    private static final FRAccountWithBalance ACCOUNTS_WITH_BALANCE = aValidFRAccountWithBalance();
    private static final String ACCOUNT_ID = ACCOUNTS_WITH_BALANCE.getId();
    private static final String OAUTH2_AUTHORIZE_PATH = "/am/oauth2/authorize";
    private static final Tpp TPP = aValidTppBuilder().clientId(CLIENT_ID).build();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WireMockStubHelper wireMockStubHelper;

    @BeforeEach
    public void setup() {
        wireMockStubHelper.stubGetUserProfile(Map.of("id", USER_ID));
        wireMockStubHelper.stubGetUserAccounts(List.of(ACCOUNTS_WITH_BALANCE));
        wireMockStubHelper.stubGetTpp(TPP);
    }

    @Test
    public void shouldGetDomesticPaymentConsentDetails() {
        // Given
        String intentId = "PDC_d79e7380-a3a8-4de3-bc93-e4ff9b620098";
        FRDomesticPaymentConsent initialConsent = aValidDomesticPaymentConsent(intentId, null);
        wireMockStubHelper.stubGetPaymentConsent(initialConsent);
        FRDomesticPaymentConsent authorisedConsent = aValidDomesticPaymentConsent(intentId, USER_ID);
        wireMockStubHelper.stubUpdatePaymentConsent(authorisedConsent);
        String consentJwt = consentRequestJwt(OAUTH2_AUTHORIZE_PATH, 9080, CLIENT_ID, intentId, USER_ID);
        HttpEntity<String> request = new HttpEntity<>(consentJwt, headers());
        String url = detailsUrl();

        // When
        ResponseEntity<PaymentConsentDecision> response = restTemplate.postForEntity(url, request, PaymentConsentDecision.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldGetInternationalPaymentConsentDetails() {
        // Given
        String intentId = "PIC_d79e7380-a3a8-4de3-bc93-e4ff9b620098";
        FRInternationalPaymentConsent initialConsent = aValidInternationalPaymentConsent(intentId, null);
        wireMockStubHelper.stubGetPaymentConsent(initialConsent);
        FRInternationalPaymentConsent authorisedConsent = aValidInternationalPaymentConsent(intentId, USER_ID);
        wireMockStubHelper.stubUpdatePaymentConsent(authorisedConsent);
        String consentJwt = consentRequestJwt(OAUTH2_AUTHORIZE_PATH, 9080, CLIENT_ID, intentId, USER_ID);
        HttpEntity<String> request = new HttpEntity<>(consentJwt, headers());
        String url = detailsUrl();

        // When
        ResponseEntity<PaymentConsentDecision> response = restTemplate.postForEntity(url, request, PaymentConsentDecision.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldGetFilePaymentConsentDetails() {
        // Given
        String intentId = "PFC_d79e7380-a3a8-4de3-bc93-e4ff9b620098";
        FRFilePaymentConsent initialConsent = aValidFilePaymentConsent(intentId, null);
        wireMockStubHelper.stubGetPaymentConsent(initialConsent);
        FRFilePaymentConsent authorisedConsent = aValidFilePaymentConsent(intentId, USER_ID);
        wireMockStubHelper.stubUpdatePaymentConsent(authorisedConsent);
        String consentJwt = consentRequestJwt(OAUTH2_AUTHORIZE_PATH, 9080, CLIENT_ID, intentId, USER_ID);
        HttpEntity<String> request = new HttpEntity<>(consentJwt, headers());
        String url = detailsUrl();

        // When
        ResponseEntity<PaymentConsentDecision> response = restTemplate.postForEntity(url, request, PaymentConsentDecision.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldGetFundsConfirmationConsentDetails() {
        // Given
        String intentId = "FCC_d79e7380-a3a8-4de3-bc93-e4ff9b620098";
        FRFundsConfirmationConsent initialConsent = aValidFundsConfirmationConsent(intentId, null);
        wireMockStubHelper.stubGetPaymentConsent(initialConsent);
        FRFundsConfirmationConsent authorisedConsent = aValidFundsConfirmationConsent(intentId, USER_ID);
        wireMockStubHelper.stubUpdatePaymentConsent(authorisedConsent);
        String consentJwt = consentRequestJwt(OAUTH2_AUTHORIZE_PATH, 9080, CLIENT_ID, intentId, USER_ID);
        HttpEntity<String> request = new HttpEntity<>(consentJwt, headers());
        String url = detailsUrl();

        // When
        ResponseEntity<PaymentConsentDecision> response = restTemplate.postForEntity(url, request, PaymentConsentDecision.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        headers.add("Cookie", "iPlanetDirectoryPro=aSsoToken");
        return headers;
    }

    private String detailsUrl() {
        return BASE_URL + port + DETAILS_URI;
    }

    private FRDomesticPaymentConsent aValidDomesticPaymentConsent(String intentId, String resourceOwnerUsername) {
        String debitIdentification = ACCOUNTS_WITH_BALANCE.getAccount().getAccounts().get(0).getIdentification();
        return aValidFRDomesticPaymentConsentBuilder()
                .id(intentId)
                .data(aValidDomesticPaymentConsentDataBuilder(intentId)
                        .initiation(aValidOBWriteDomestic2DataInitiation()
                                .debtorAccount(aValidOBWriteDomestic2DataInitiationDebtorAccount()
                                        .identification(debitIdentification)))
                        .build())
                .accountId(ACCOUNT_ID)
                .oauth2ClientId(TPP.getClientId())
                .oauth2ClientName(TPP.getName())
                .resourceOwnerUsername(resourceOwnerUsername)
                .build();
    }

    private FRInternationalPaymentConsent aValidInternationalPaymentConsent(String intentId, String resourceOwnerUsername) {
        String debitIdentification = ACCOUNTS_WITH_BALANCE.getAccount().getAccounts().get(0).getIdentification();
        return aValidFRInternationalPaymentConsentBuilder()
                .id(intentId)
                .data(aValidInternationalPaymentConsentDataBuilder(intentId)
                        .initiation(aValidOBWriteInternational3DataInitiation()
                                .debtorAccount(aValidOBWriteDomestic2DataInitiationDebtorAccount()
                                        .identification(debitIdentification)))
                        .build())
                .accountId(ACCOUNT_ID)
                .oauth2ClientId(TPP.getClientId())
                .oauth2ClientName(TPP.getName())
                .resourceOwnerUsername(resourceOwnerUsername)
                .build();
    }

    private FRFilePaymentConsent aValidFilePaymentConsent(String intentId, String resourceOwnerUsername) {
        String debitIdentification = ACCOUNTS_WITH_BALANCE.getAccount().getAccounts().get(0).getIdentification();
        return aValidFRFilePaymentConsentBuilder()
                .id(intentId)
                .data(aValidFilePaymentConsentDataBuilder(intentId)
                        .initiation(aValidOBWriteFile2DataInitiation()
                                .debtorAccount(aValidOBWriteDomestic2DataInitiationDebtorAccount()
                                        .identification(debitIdentification)))
                        .build())
                .accountId(ACCOUNT_ID)
                .oauth2ClientId(TPP.getClientId())
                .oauth2ClientName(TPP.getName())
                .resourceOwnerUsername(resourceOwnerUsername)
                .build();
    }

    private FRFundsConfirmationConsent aValidFundsConfirmationConsent(String intentId, String resourceOwnerUsername) {
        String debitIdentification = ACCOUNTS_WITH_BALANCE.getAccount().getAccounts().get(0).getIdentification();
        return aValidFRFundsConfirmationConsentBuilder()
                .id(intentId)
                .data(aValidFundsConfirmationConsentDataBuilder(intentId)
                        .debtorAccount(aValidOBCashAccount3()
                                .identification(debitIdentification))
                        .build())
                .accountId(ACCOUNT_ID)
                .oauth2ClientId(TPP.getClientId())
                .oauth2ClientName(TPP.getName())
                .resourceOwnerUsername(resourceOwnerUsername)
                .build();
    }
}