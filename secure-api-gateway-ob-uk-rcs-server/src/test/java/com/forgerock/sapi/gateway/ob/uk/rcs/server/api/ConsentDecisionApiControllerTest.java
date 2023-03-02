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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api;

import com.forgerock.sapi.gateway.ob.uk.rcs.server.RCSServerApplicationTestSupport;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.testsupport.JwtTestHelper;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDecisionRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ConsentServiceClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Stream;

import static com.forgerock.sapi.gateway.ob.uk.rcs.server.util.Constants.*;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.ConsentDecisionRequestTestDataFactory.aValidAccountConsentClientDecisionRequest;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.ConsentDecisionRequestTestDataFactory.aValidDomesticPaymentConsentClientDecisionRequest;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticPaymentConsentDetailsTestFactory.aValidDomesticPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticScheduledPaymentConsentDetailsTestFactory.aValidDomesticScheduledPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticStandingOrderConsentDetailsTestFactory.aValidDomesticStandingOrderConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory.aValidDomesticVrpPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.FilePaymentConsentDetailsTestFactory.aValidFilePaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.InternationalPaymentConsentDetailsTestFactory.aValidInternationalPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.InternationalScheduledPaymentConsentDetailsTestFactory.aValidInternationalScheduledPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.InternationalStandingOrderConsentDetailsTestFactory.aValidInternationalStandingOrderConsentDetails;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Spring Boot Test for {@link ConsentDecisionApiController}.
 */
@EnableConfigurationProperties
@ActiveProfiles("test")
@SpringBootTest(classes = RCSServerApplicationTestSupport.class, webEnvironment = RANDOM_PORT)
public class ConsentDecisionApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String CONTEXT_DETAILS_URI = "/rcs/api/consent/decision";
    @LocalServerPort
    private int port;
    @MockBean
    private ConsentServiceClient consentServiceClient;
    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${rcs.consent.response.jwt.signingKeyId}")
    private String expectedSigningKeyId;

    @Value("${rcs.consent.response.jwt.signingAlgorithm}")
    private String expectedSigningAlgorithm;

    @Value("${rcs.consent.response.jwt.issuer}")
    private String expectedConsentResponseJwtIssuer;

    private JWSVerifier jwsVerifier;

    public ConsentDecisionApiControllerTest(@Value("${rcs.consent.response.jwt.privateKeyPath}") Path privateKeyPath) throws Exception {
        final JWK jwk = JWK.parseFromPEMEncodedObjects(Files.readString(privateKeyPath));
        jwsVerifier = new RSASSAVerifier((RSAKey) jwk);
    }

    @Test
    public void ShouldGetAccountsRedirectAction(
    ) throws ExceptionClient {
        // Given
        ConsentClientDecisionRequest consentClientDecisionRequest = aValidAccountConsentClientDecisionRequest(ACCOUNT_INTENT_ID);
        JsonObject jsonObjectConsentDetails = aValidAccountConsentDetails(consentClientDecisionRequest.getIntentId());
        given(consentServiceClient.updateConsent(consentClientDecisionRequest)).willReturn(jsonObjectConsentDetails);
        String jwt = JwtTestHelper.consentRequestJwt(
                consentClientDecisionRequest.getClientId(),
                consentClientDecisionRequest.getIntentId(),
                consentClientDecisionRequest.getResourceOwnerUsername()
        );

        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;

        ConsentDecisionDeserialized consentDecisionDeserialized = ConsentDecisionDeserialized.builder()
                .accountIds(
                        new Gson().fromJson(jsonObjectConsentDetails.getAsJsonArray("accountIds"), List.class)
                )
                .consentJwt(jwt)
                .decision(Constants.ConsentDecisionStatus.AUTHORISED)
                .build();
        HttpEntity request = new HttpEntity(consentDecisionDeserialized, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        final String consentJwt = response.getBody().getConsentJwt();
        assertThat(consentJwt).isNotEmpty();
        verifyConsentResponseJwt(consentJwt);
        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
    }

    private static Stream<Arguments> validArgumentsForPayments() {
        return Stream.of(
                arguments(
                        "DOMESTIC PAYMENT",
                        aValidDomesticPaymentConsentClientDecisionRequest(DOMESTIC_PAYMENT_INTENT_ID),
                        aValidDomesticPaymentConsentDetails(DOMESTIC_PAYMENT_INTENT_ID)
                ),
                arguments(
                        "DOMESTIC SCHEDULED",
                        aValidDomesticPaymentConsentClientDecisionRequest(DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID),
                        aValidDomesticScheduledPaymentConsentDetails(DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID)
                ),
                arguments(
                        "DOMESTIC STANDING ORDER",
                        aValidDomesticPaymentConsentClientDecisionRequest(DOMESTIC_STANDING_ORDER_INTENT_ID),
                        aValidDomesticStandingOrderConsentDetails(DOMESTIC_STANDING_ORDER_INTENT_ID)
                ),
                arguments(
                        "INTERNATIONAL PAYMENT",
                        aValidDomesticPaymentConsentClientDecisionRequest(INTERNATIONAL_PAYMENT_INTENT_ID),
                        aValidInternationalPaymentConsentDetails(INTERNATIONAL_PAYMENT_INTENT_ID)
                ),
                arguments(
                        "INTERNATIONAL SCHEDULED",
                        aValidDomesticPaymentConsentClientDecisionRequest(INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID),
                        aValidInternationalScheduledPaymentConsentDetails(INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID)
                ),
                arguments(
                        "INTERNATIONAL STANDING ORDER",
                        aValidDomesticPaymentConsentClientDecisionRequest(INTERNATIONAL_STANDING_ORDER_INTENT_ID),
                        aValidInternationalStandingOrderConsentDetails(INTERNATIONAL_STANDING_ORDER_INTENT_ID)
                ),
                arguments(
                        "FILE PAYMENT",
                        aValidDomesticPaymentConsentClientDecisionRequest(FILE_PAYMENT_INTENT_ID),
                        aValidFilePaymentConsentDetails(FILE_PAYMENT_INTENT_ID)
                ),
                arguments(
                        "VRP PAYMENT",
                        aValidDomesticPaymentConsentClientDecisionRequest(DOMESTIC_VRP_PAYMENT_INTENT_ID),
                        aValidDomesticVrpPaymentConsentDetails(DOMESTIC_VRP_PAYMENT_INTENT_ID)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("validArgumentsForPayments")
    public void ShouldGetPaymentRedirectAction(
            String testIdName,
            ConsentClientDecisionRequest consentClientDecisionRequest,
            JsonObject jsonObjectConsentDetails
    ) throws ExceptionClient {
        // Given
        given(consentServiceClient.updateConsent(consentClientDecisionRequest)).willReturn(jsonObjectConsentDetails);
        String jwt = JwtTestHelper.consentRequestJwt(
                consentClientDecisionRequest.getClientId(),
                consentClientDecisionRequest.getIntentId(),
                consentClientDecisionRequest.getResourceOwnerUsername()
        );

        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;

        FRAccountIdentifier accountIdentifier = new FRAccountIdentifier();
        accountIdentifier.setIdentification("76064512389965");
        accountIdentifier.setName("John");
        accountIdentifier.setSchemeName("UK.OBIE.SortCodeAccountNumber");
        FRFinancialAccount financialAccount = new FRFinancialAccount();
        financialAccount.setAccounts(List.of(accountIdentifier));
        financialAccount.setAccountId("30ff5da7-7d0f-43fe-974c-7b34717cbeec");

        ConsentDecisionDeserialized consentDecisionRequest = ConsentDecisionDeserialized.builder()
                .consentJwt(jwt)
                .decision(Constants.ConsentDecisionStatus.AUTHORISED)
                .debtorAccount(financialAccount)
                .build();
        HttpEntity<String> request = new HttpEntity(consentDecisionRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        final String consentJwt = response.getBody().getConsentJwt();
        assertThat(consentJwt).isNotEmpty();
        verifyConsentResponseJwt(consentJwt);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
    }

    @Test
    public void ShouldRedirectActionWrongIntentError() {
        // Given
        ConsentClientDecisionRequest consentClientDecisionRequest = aValidDomesticPaymentConsentClientDecisionRequest("WRONG_INTENT_ID");
        String jwt = JwtTestHelper.consentRequestJwt(
                consentClientDecisionRequest.getClientId(),
                consentClientDecisionRequest.getIntentId(),
                consentClientDecisionRequest.getResourceOwnerUsername()
        );
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        ConsentDecisionDeserialized consentDecisionRequest = ConsentDecisionDeserialized.builder()
                .consentJwt(jwt)
                .decision(Constants.ConsentDecisionStatus.AUTHORISED)
                .build();
        HttpEntity<String> request = new HttpEntity(consentDecisionRequest, headers());
        // When
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);
        // Then
        assertEquals(ErrorType.UNKNOWN_INTENT_TYPE.getHttpStatus(), response.getStatusCode());
        assertEquals(ErrorType.UNKNOWN_INTENT_TYPE.getDescription(), response.getBody().getErrorMessage());
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        headers.add("Cookie", "iPlanetDirectoryPro=aSsoToken");
        return headers;
    }

    private void verifyConsentResponseJwt(String consentResponseJwt) {
        assertNotNull(consentResponseJwt);
        try {
            final JWSObject parsedConsent = JWSObject.parse(consentResponseJwt);
            assertEquals(expectedSigningAlgorithm, parsedConsent.getHeader().getAlgorithm().getName());
            assertEquals(expectedSigningKeyId, parsedConsent.getHeader().getKeyID());
            final JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(parsedConsent.getPayload().toJSONObject());
            assertEquals(expectedConsentResponseJwtIssuer, jwtClaimsSet.getIssuer());
            assertTrue(parsedConsent.verify(jwsVerifier), "consentResponseJwt sig failed validation");
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
