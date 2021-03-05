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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRDomesticPaymentConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRDomesticPaymentConsent.FRDomesticPaymentConsentBuilder;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.DomesticPaymentConsentDecision;
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

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRConsentStatusCode.AWAITINGAUTHORISATION;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.payment.FRDomesticPaymentConsentTestDataFactory.aValidFRDomesticPaymentConsentBuilder;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.tpp.TppTestDataFactory.aValidTppBuilder;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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
    private static final String CLIENT_ID = "fe061f12-135a-44f4-be23-f0a4a5c23eea";
    private static final String USER_ID = "45c6486e-8fc0-3ffc-h6f5-2105164d01j4";
    private static final FRAccountWithBalance ACCOUNTS_WITH_BALANCE = aValidFRAccountWithBalance();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WireMockStubHelper wireMockStubHelper;

    @BeforeEach
    public void setup() {
        Tpp tpp = aValidTppBuilder().clientId(CLIENT_ID).build();
        FRDomesticPaymentConsentBuilder paymentConsentBuilder = frDomesticPaymentConsentBuilder(tpp);

        wireMockStubHelper.stubGetUserProfile(Map.of("id", USER_ID));
        wireMockStubHelper.stubGetUserAccounts(List.of(ACCOUNTS_WITH_BALANCE));
        wireMockStubHelper.stubGetPaymentConsent(paymentConsentBuilder.build());
        wireMockStubHelper.stubGetTpp(tpp);

        FRDomesticPaymentConsent expectedPaymentConsent = paymentConsentBuilder.userId(USER_ID).build();
        wireMockStubHelper.stubUpdatePaymentConsent(expectedPaymentConsent);
    }

    @Test
    public void shouldGetDomesticPaymentConsentDetails() {
        // Given
        HttpEntity<String> request = new HttpEntity<>(consentRequestJwt(), headers());
        String url = detailsUrl();

        // When
        ResponseEntity<DomesticPaymentConsentDecision> response = restTemplate.postForEntity(url, request, DomesticPaymentConsentDecision.class);

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

    private FRDomesticPaymentConsentBuilder frDomesticPaymentConsentBuilder(Tpp tpp) {
        return aValidFRDomesticPaymentConsentBuilder()
                .pispId(tpp.getClientId())
                .pispName(tpp.getName())
                .userId(null)
                .status(AWAITINGAUTHORISATION);
    }

    private String consentRequestJwt() {
        return "eyJraWQiOiI0ZTdmMTQ0MzI5ZDc2ZGZlY2UwNmFiZTAyZDJmZGIyMGYwZjJhYjViIiwiYWxnIjoiUFMyNTYifQ." +
                "eyJpc3MiOiJmZTA2MWYxMi0xMzVhLTQ0ZjQtYmUyMy1mMGE0YTVjMjNlZWEiLCJyZXNwb25zZV90eXBlIjoiY29kZSBpZF90b2" +
                "tlbiIsIm5vbmNlIjoiMTBkMjYwYmYtYTdkOS00NDRhLTkyZDktN2I3YTVmMDg4MjA4IiwiY2xpZW50X2lkIjoiZmUwNjFmMTIt" +
                "MTM1YS00NGY0LWJlMjMtZjBhNGE1YzIzZWVhIiwiYXVkIjoiaHR0cHM6XC9cL2FzLmFzcHNwLmRldi1vYi5mb3JnZXJvY2suZm" +
                "luYW5jaWFsOjgwNzRcL29hdXRoMiIsInNjb3BlIjoib3BlbmlkIGFjY291bnRzIHBheW1lbnRzIiwiY2xhaW1zIjp7ImlkX3Rv" +
                "a2VuIjp7ImFjciI6eyJ2YWx1ZSI6InVybjpvcGVuYmFua2luZzpwc2QyOnNjYSIsImVzc2VudGlhbCI6dHJ1ZX0sIm9wZW5iYW" +
                "5raW5nX2ludGVudF9pZCI6eyJ2YWx1ZSI6IlBEQ19hZTMwZjNkMC05ZGM3LTRhZDAtYjU4ZS1iYzFmNzBlMjBkZDEiLCJlc3Nl" +
                "bnRpYWwiOnRydWV9fSwidXNlcmluZm8iOnsib3BlbmJhbmtpbmdfaW50ZW50X2lkIjp7InZhbHVlIjoiUERDX2FlMzBmM2QwLT" +
                "lkYzctNGFkMC1iNThlLWJjMWY3MGUyMGRkMSIsImVzc2VudGlhbCI6dHJ1ZX19fSwicmVkaXJlY3RfdXJpIjoiaHR0cHM6XC9c" +
                "L3d3dy5nb29nbGUuY29tIiwic3RhdGUiOiIxMGQyNjBiZi1hN2Q5LTQ0NGEtOTJkOS03YjdhNWYwODgyMDgiLCJleHAiOjE2MT" +
                "UyMTc2NTQsImlhdCI6MTYxNTIxNzM1NCwianRpIjoiOTNjN2JmZTEtMGY1ZC00YmJhLTk0ZmItODRjNTg3MTg1MTI1In0." +
                "HVtWvts0a9eSfU3TsKodZiKCFoySzX1Tsiv_UJp07ZFDlpxeZn2VUIlwHDvI_z5AnUNxOi_Tp7zMMOq-BIZEF-cTWV0uBxTPiM" +
                "K9gJxZf9zYQiY-wD4eLoZjofbTWWoQR2jncdB2qyQ-gB8WXTtIYat4v4F7r1Dw-bfoIWWJTQBoydYGHmTRUSTCvU8AHDTD8jwi" +
                "q4WdtWDkQPBV2fqJGeCtrm4rO8CLv9jOFo0DbXCT2WctccbUbnaHYPfoLp_sxkB_apNseZe0s9BJI6anLx6kPSItQhF09eqdP2" +
                "V7p1b1g6bJNBqIQZjlAV-_ogvM3CDZHCBdY4ElKD6FqCTppA";
    }
}