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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.PaymentConsentDecision;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.*;
import com.forgerock.securebanking.openbanking.uk.rcs.testsupport.TestInitializer;
import com.forgerock.securebanking.openbanking.uk.rcs.testsupport.WireMockServerExtension;
import com.forgerock.securebanking.openbanking.uk.rcs.testsupport.WireMockStubHelper;
import lombok.SneakyThrows;
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
import static com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode.AUTHORISED;
import static com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode.AWAITINGAUTHORISATION;
import static com.forgerock.securebanking.openbanking.uk.rcs.common.RcsConstants.Decision.ALLOW;
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

/**
 * Spring Boot Test for {@link ConsentDecisionApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = TestInitializer.class)
@ActiveProfiles("test")
@ExtendWith(WireMockServerExtension.class)
public class ConsentDecisionApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String DECISION_URI = "/api/rcs/consent/decision";
    private static final String CLIENT_ID = "fe061f12-135a-44f4-be23-f0a4a5c23eea";
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
    private ObjectMapper objectMapper;

    @Autowired
    private WireMockStubHelper wireMockStubHelper;

    private String consentJwt;

    @BeforeEach
    public void setup() {
        wireMockStubHelper.stubGetUserProfile(Map.of("id", USER_ID));
        wireMockStubHelper.stubGetUserAccounts(List.of(ACCOUNTS_WITH_BALANCE));
        wireMockStubHelper.stubGetTpp(TPP);
        wireMockStubHelper.stubRcsResponseToAm(OAUTH2_AUTHORIZE_PATH);
    }

    @Test
    public void shouldSubmitDomesticPaymentConsentDecision() {
        // Given
        String intentId = "PDC_d79e7380-a3a8-4de3-bc93-e4ff9b620098";
        FRDomesticPaymentConsent initialConsent = aValidDomesticPaymentConsent(intentId, AWAITINGAUTHORISATION);
        wireMockStubHelper.stubGetPaymentConsent(initialConsent);
        consentJwt = consentRequestJwt(OAUTH2_AUTHORIZE_PATH, 9080, CLIENT_ID, intentId, USER_ID);
        wireMockStubHelper.stubSignClaims(consentJwt);
        FRDomesticPaymentConsent authorisedConsent = aValidDomesticPaymentConsent(intentId, AUTHORISED);
        wireMockStubHelper.stubUpdatePaymentConsent(authorisedConsent);
        HttpEntity<String> request = new HttpEntity<>(consentDecisionSerialised(), headers());
        String url = decisionUrl();

        // When
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(url, request, RedirectionAction.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldSubmitInternationalPaymentConsentDecision() {
        // Given
        String intentId = "PIC_d79e7380-a3a8-4de3-bc93-e4ff9b620098";
        FRInternationalPaymentConsent initialConsent = aValidInternationalPaymentConsent(intentId, AWAITINGAUTHORISATION);
        wireMockStubHelper.stubGetPaymentConsent(initialConsent);
        consentJwt = consentRequestJwt(OAUTH2_AUTHORIZE_PATH, 9080, CLIENT_ID, intentId, USER_ID);
        wireMockStubHelper.stubSignClaims(consentJwt);
        FRInternationalPaymentConsent authorisedConsent = aValidInternationalPaymentConsent(intentId, AUTHORISED);
        wireMockStubHelper.stubUpdatePaymentConsent(authorisedConsent);
        HttpEntity<String> request = new HttpEntity<>(consentDecisionSerialised(), headers());
        String url = decisionUrl();

        // When
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(url, request, RedirectionAction.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldSubmitFilePaymentConsentDecision() {
        // Given
        String intentId = "PFC_d79e7380-a3a8-4de3-bc93-e4ff9b620098";
        FRFilePaymentConsent initialConsent = aValidFilePaymentConsent(intentId, AWAITINGAUTHORISATION);
        wireMockStubHelper.stubGetPaymentConsent(initialConsent);
        consentJwt = consentRequestJwt(OAUTH2_AUTHORIZE_PATH, 9080, CLIENT_ID, intentId, USER_ID);
        wireMockStubHelper.stubSignClaims(consentJwt);
        FRFilePaymentConsent authorisedConsent = aValidFilePaymentConsent(intentId, AUTHORISED);
        wireMockStubHelper.stubUpdatePaymentConsent(authorisedConsent);
        HttpEntity<String> request = new HttpEntity<>(consentDecisionSerialised(), headers());
        String url = decisionUrl();

        // When
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(url, request, RedirectionAction.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldSubmitFundsConfirmationConsentDecision() {
        // Given
        String intentId = "FCC_d79e7380-a3a8-4de3-bc93-e4ff9b620098";
        FRFundsConfirmationConsent initialConsent = aValidFundsConfirmationConsent(intentId, AWAITINGAUTHORISATION);
        wireMockStubHelper.stubGetPaymentConsent(initialConsent);
        consentJwt = consentRequestJwt(OAUTH2_AUTHORIZE_PATH, 9080, CLIENT_ID, intentId, USER_ID);
        wireMockStubHelper.stubSignClaims(consentJwt);
        FRFundsConfirmationConsent authorisedConsent = aValidFundsConfirmationConsent(intentId, AUTHORISED);
        wireMockStubHelper.stubUpdatePaymentConsent(authorisedConsent);
        HttpEntity<String> request = new HttpEntity<>(consentDecisionSerialised(), headers());
        String url = decisionUrl();

        // When
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(url, request, RedirectionAction.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    @SneakyThrows
    private String consentDecisionSerialised() {
        return objectMapper.writeValueAsString(PaymentConsentDecision.builder()
                .consentJwt(consentJwt)
                .decision(ALLOW)
                .accountId(ACCOUNT_ID)
                .build());
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        headers.add("Cookie", "iPlanetDirectoryPro=aSsoToken");
        return headers;
    }

    private String decisionUrl() {
        return BASE_URL + port + DECISION_URI;
    }

    private FRDomesticPaymentConsent aValidDomesticPaymentConsent(String intentId, FRConsentStatusCode status) {
        return aValidFRDomesticPaymentConsentBuilder()
                .id(intentId)
                .data(aValidDomesticPaymentConsentDataBuilder(intentId).status(status).build())
                .accountId(ACCOUNT_ID)
                .oauth2ClientId(TPP.getClientId())
                .oauth2ClientName(TPP.getName())
                .resourceOwnerUsername(USER_ID)
                .build();
    }

    private FRInternationalPaymentConsent aValidInternationalPaymentConsent(String intentId, FRConsentStatusCode status) {
        return aValidFRInternationalPaymentConsentBuilder()
                .id(intentId)
                .data(aValidInternationalPaymentConsentDataBuilder(intentId).status(status).build())
                .accountId(ACCOUNT_ID)
                .oauth2ClientId(TPP.getClientId())
                .oauth2ClientName(TPP.getName())
                .resourceOwnerUsername(USER_ID)
                .build();
    }

    private FRFilePaymentConsent aValidFilePaymentConsent(String intentId, FRConsentStatusCode status) {
        return aValidFRFilePaymentConsentBuilder()
                .id(intentId)
                .data(aValidFilePaymentConsentDataBuilder(intentId).status(status).build())
                .accountId(ACCOUNT_ID)
                .oauth2ClientId(TPP.getClientId())
                .oauth2ClientName(TPP.getName())
                .resourceOwnerUsername(USER_ID)
                .build();
    }

    private FRFundsConfirmationConsent aValidFundsConfirmationConsent(String intentId, FRConsentStatusCode status) {
        return aValidFRFundsConfirmationConsentBuilder()
                .id(intentId)
                .data(aValidFundsConfirmationConsentDataBuilder(intentId).status(status).build())
                .accountId(ACCOUNT_ID)
                .oauth2ClientId(TPP.getClientId())
                .oauth2ClientName(TPP.getName())
                .resourceOwnerUsername(USER_ID)
                .build();
    }
}