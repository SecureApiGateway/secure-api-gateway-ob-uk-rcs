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
package com.forgerock.securebanking.openbanking.uk.rcs.api;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAccountIdentifier;
import com.forgerock.securebanking.openbanking.uk.rcs.RcsApplicationTestSupport;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.securebanking.openbanking.uk.rcs.mapper.decision.ConsentDecisionMapper;
import com.forgerock.securebanking.openbanking.uk.rcs.testsupport.JwtTestHelper;
import com.forgerock.securebanking.platform.client.Constants;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.ConsentClientDecisionRequest;
import com.forgerock.securebanking.platform.client.services.ConsentServiceClient;
import com.forgerock.securebanking.platform.client.services.JwkServiceClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Stream;

import static com.forgerock.securebanking.openbanking.uk.rcs.util.Constants.*;
import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.ConsentDecisionRequestTestDataFactory.aValidAccountConsentClientDecisionRequest;
import static com.forgerock.securebanking.platform.client.test.support.ConsentDecisionRequestTestDataFactory.aValidDomesticPaymentConsentClientDecisionRequest;
import static com.forgerock.securebanking.platform.client.test.support.DomesticPaymentConsentDetailsTestFactory.aValidDomesticPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.DomesticScheduledPaymentConsentDetailsTestFactory.aValidDomesticScheduledPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.DomesticStandingOrderConsentDetailsTestFactory.aValidDomesticStandingOrderConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory.aValidDomesticVrpPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.FilePaymentConsentDetailsTestFactory.aValidFilePaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.InternationalPaymentConsentDetailsTestFactory.aValidInternationalPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.InternationalScheduledPaymentConsentDetailsTestFactory.aValidInternationalScheduledPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.InternationalStandingOrderConsentDetailsTestFactory.aValidInternationalStandingOrderConsentDetails;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Spring Boot Test for {@link ConsentDecisionApiController}.
 */
@EnableConfigurationProperties
@ActiveProfiles("test")
@SpringBootTest(classes = RcsApplicationTestSupport.class, webEnvironment = RANDOM_PORT)
public class ConsentDecisionApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String CONTEXT_DETAILS_URI = "/api/rcs/consent/decision";
    @LocalServerPort
    private int port;
    @MockBean
    private ConsentServiceClient consentServiceClient;

    @MockBean
    private JwkServiceClient jwkServiceClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    ConsentDecisionMapper consentDecisionMapper;

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

        given(jwkServiceClient.signClaims(any(JWTClaimsSet.class), anyString())).willReturn(jwt);
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
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
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

        given(jwkServiceClient.signClaims(any(JWTClaimsSet.class), anyString())).willReturn(jwt);
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
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
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
}
