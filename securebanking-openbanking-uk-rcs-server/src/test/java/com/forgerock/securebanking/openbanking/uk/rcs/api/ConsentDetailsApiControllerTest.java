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
    private static final String CLIENT_ID = "a12f9ebc-4966-4543-afe0-03e597835a01";
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
        return "eyJ0eXAiOiJKV1QiLCJraWQiOiJ3VTNpZklJYUxPVUFSZVJCL0ZHNmVNMVAxUU09IiwiYWxnIjoiUFMyNTYifQ." +
                "eyJjbGllbnRJZCI6ImExMmY5ZWJjLTQ5NjYtNDU0My1hZmUwLTAzZTU5NzgzNWEwMSIsImlzcyI6Imh0dHBzOi8vYXMuYXNwc3AuZGV2LW9iLmZvcmdlcm9jay5maW5hbmNpYWw6ODA3NC9vYXV0aDIiLCJjc3JmIjoiQlBWUmxvUHFqS2YrcWQxUHVscHFESHFiMEF1aWRZaGxhVWE5ZjVia2hnYz0iLCJjbGllbnRfZGVzY3JpcHRpb24iOiIiLCJhdWQiOiJmb3JnZXJvY2stcmNzIiwic2F2ZV9jb25zZW50X2VuYWJsZWQiOnRydWUsImNsYWltcyI6eyJpZF90b2tlbiI6eyJhY3IiOnsidmFsdWUiOiJ1cm46b3BlbmJhbmtpbmc6cHNkMjpzY2EiLCJlc3NlbnRpYWwiOnRydWV9LCJvcGVuYmFua2luZ19pbnRlbnRfaWQiOnsidmFsdWUiOiJQRENfZDkxYTU3MzgtODMwZi00NDdhLTliYzktMGVmNDYzN2UzM2MzIiwiZXNzZW50aWFsIjp0cnVlfX0sInVzZXJpbmZvIjp7Im9wZW5iYW5raW5nX2ludGVudF9pZCI6eyJ2YWx1ZSI6IlBEQ19kOTFhNTczOC04MzBmLTQ0N2EtOWJjOS0wZWY0NjM3ZTMzYzMiLCJlc3NlbnRpYWwiOnRydWV9fX0sInNjb3BlcyI6eyJhY2NvdW50cyI6ImFjY291bnRzIiwib3BlbmlkIjoib3BlbmlkIiwicGF5bWVudHMiOiJwYXltZW50cyJ9LCJleHAiOjE2MTYwODAwNDEsImlhdCI6MTYxNjA3OTg2MSwiY2xpZW50X25hbWUiOiJNYXR0J3MgVFBQIiwiY29uc2VudEFwcHJvdmFsUmVkaXJlY3RVcmkiOiJodHRwczovL2FzLmFzcHNwLmRldi1vYi5mb3JnZXJvY2suZmluYW5jaWFsOjgwNzQvb2F1dGgyL2F1dGhvcml6ZT9yZXNwb25zZV90eXBlPWNvZGUraWRfdG9rZW4mY2xpZW50X2lkPWExMmY5ZWJjLTQ5NjYtNDU0My1hZmUwLTAzZTU5NzgzNWEwMSZzdGF0ZT0xMGQyNjBiZi1hN2Q5LTQ0NGEtOTJkOS03YjdhNWYwODgyMDgmbm9uY2U9MTBkMjYwYmYtYTdkOS00NDRhLTkyZDktN2I3YTVmMDg4MjA4JnNjb3BlPW9wZW5pZCtwYXltZW50cythY2NvdW50cyZyZWRpcmVjdF91cmk9aHR0cHMlM0ElMkYlMkZ3d3cuZ29vZ2xlLmNvbSZyZXF1ZXN0PWV5SnJhV1FpT2lKa016WTJOalUxWmpjME16YzJPRFl6TURjNU16Y3lNR1UwWW1NeU1HUTBNekV5WlRZd09UZ3dJaXdpWVd4bklqb2lVRk15TlRZaWZRLmV5SnBjM01pT2lKaE1USm1PV1ZpWXkwME9UWTJMVFExTkRNdFlXWmxNQzB3TTJVMU9UYzRNelZoTURFaUxDSnlaWE53YjI1elpWOTBlWEJsSWpvaVkyOWtaU0JwWkY5MGIydGxiaUlzSW01dmJtTmxJam9pTVRCa01qWXdZbVl0WVRka09TMDBORFJoTFRreVpEa3ROMkkzWVRWbU1EZzRNakE0SWl3aVkyeHBaVzUwWDJsa0lqb2lZVEV5WmpsbFltTXRORGsyTmkwME5UUXpMV0ZtWlRBdE1ETmxOVGszT0RNMVlUQXhJaXdpWVhWa0lqb2lhSFIwY0hNNlhDOWNMMkZ6TG1GemNITndMbVJsZGkxdllpNW1iM0puWlhKdlkyc3VabWx1WVc1amFXRnNPamd3TnpSY0wyOWhkWFJvTWlJc0luTmpiM0JsSWpvaWIzQmxibWxrSUdGalkyOTFiblJ6SUhCaGVXMWxiblJ6SWl3aVkyeGhhVzF6SWpwN0ltbGtYM1J2YTJWdUlqcDdJbUZqY2lJNmV5SjJZV3gxWlNJNkluVnlianB2Y0dWdVltRnVhMmx1Wnpwd2MyUXlPbk5qWVNJc0ltVnpjMlZ1ZEdsaGJDSTZkSEoxWlgwc0ltOXdaVzVpWVc1cmFXNW5YMmx1ZEdWdWRGOXBaQ0k2ZXlKMllXeDFaU0k2SWxCRVExOWtPVEZoTlRjek9DMDRNekJtTFRRME4yRXRPV0pqT1Mwd1pXWTBOak0zWlRNell6TWlMQ0psYzNObGJuUnBZV3dpT25SeWRXVjlmU3dpZFhObGNtbHVabThpT25zaWIzQmxibUpoYm10cGJtZGZhVzUwWlc1MFgybGtJanA3SW5aaGJIVmxJam9pVUVSRFgyUTVNV0UxTnpNNExUZ3pNR1l0TkRRM1lTMDVZbU01TFRCbFpqUTJNemRsTXpOak15SXNJbVZ6YzJWdWRHbGhiQ0k2ZEhKMVpYMTlmU3dpY21Wa2FYSmxZM1JmZFhKcElqb2lhSFIwY0hNNlhDOWNMM2QzZHk1bmIyOW5iR1V1WTI5dElpd2ljM1JoZEdVaU9pSXhNR1F5TmpCaVppMWhOMlE1TFRRME5HRXRPVEprT1MwM1lqZGhOV1l3T0RneU1EZ2lMQ0psZUhBaU9qRTJNVFl3T0RBeE16VXNJbWxoZENJNk1UWXhOakEzT1Rnek5Td2lhblJwSWpvaVpUTmpNV1ptTUdFdFkyWmpOQzAwT0RoakxUaGhOMk10TlRaaVptRmhOMlZqWW1NekluMC5WRGpfUVVvVGU4a05xc0h0dGc3NEl6OXZYTUdmcmtpZ2xQbmstcHJUcVZ5WjRGd0VoMUxqdUNkTGN6NlI0ZVV6TU9YdjdGLVdoTnY1S0hvLTlfakhETFlDSlVEZGgzdC1ibVBsLW1UZEpEdWp6N1VmRVdiUks0eS1qQ2k2TDZBdkRsRWpCZUhvUzZ3aTZMUTY1SDdNV2pkTlpFbkhlSFlzUGZxMnZPYnItRjB1QWJ6elNNRlpqT2hBblRTejQtR1k4RUF2UlliMlNTbjRXckJHX3lpR2pOWjlFdGVvV0VLdmxDUVRrd05HcFV5bi1MTEZpclk5XzlnQkRFVHI3SHRObTVhZzZyanFkVUtGNVI3RUFyZS1UTE9YSE5FNVp1WGxtQXZpSUN1eUg0b25mSGZMQ1cwLW01Qmh1UEUtdGdPTEhLZmg1SllDYy04ZUZCU1h6T2ZTN3ciLCJ1c2VybmFtZSI6Im1hdHQud2lsbHMifQ." +
                "D3LNRrjok6laTUIqNxC-VIfcMSX82BDKVii2_TqYVUvXyeT99GZKAAW-ih4WOCY__kJzgP8PtSDgXqrVaNjR4yBN6loX4Tw5bTEoA7cwn7V7MUPlcHweSQPaABjogtdJQVDACH_OCryw1Yikf4lMeuq125JWvvXXMht5MsMahdNYTS-THQadxNVGHlKsSS1wrgfqI-u4FwKBuLvcApKMaxfNIiTOjrbCXaZwsVHQk6A0YriV6znkp5ptTgxXEVaxJGfoyPPMg_0_d8Iqb7gxZ73PACmEKuj3hOOyBlfd16uyLkvQ8qZjAbSj0xuevG-BpqN4BEqfwV9RZD91zxxZyQ";
    }
}