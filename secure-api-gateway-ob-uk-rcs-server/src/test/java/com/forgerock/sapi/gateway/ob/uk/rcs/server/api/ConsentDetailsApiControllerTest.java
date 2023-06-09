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

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.*;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.factory.details.ConsentDetailsFactoryProvider;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.RCSServerApplicationTestSupport;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.IntentType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.UserServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.ApiClientTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.testsupport.JwtTestHelper;
import com.google.gson.*;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.ConsentDetailsRequestTestDataFactory.aValidConsentDetailsRequest;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticPaymentConsentDetailsTestFactory.aValidDomesticPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticScheduledPaymentConsentDetailsTestFactory.aValidDomesticScheduledPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticStandingOrderConsentDetailsTestFactory.aValidDomesticStandingOrderConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory.aValidDomesticVrpPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.FilePaymentConsentDetailsTestFactory.aValidFilePaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.InternationalPaymentConsentDetailsTestFactory.aValidInternationalPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.InternationalScheduledPaymentConsentDetailsTestFactory.aValidInternationalScheduledPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.InternationalStandingOrderConsentDetailsTestFactory.aValidInternationalStandingOrderConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.UserTestDataFactory.aValidUser;
import static com.forgerock.sapi.gateway.ob.uk.rcs.server.api.ConsentDetailsTestValidations.validateDomesticScheduledConsentDetailsResponse;
import static com.forgerock.sapi.gateway.ob.uk.rcs.server.api.ConsentDetailsTestValidations.validateDomesticStandingOrderConsentDetailsResponse;
import static com.forgerock.sapi.gateway.ob.uk.rcs.server.util.Constants.*;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Spring Boot Test for {@link ConsentDetailsApiController} Domestic payment consent details case
 */
@EnableConfigurationProperties
@ActiveProfiles("test")
@SpringBootTest(classes = RCSServerApplicationTestSupport.class, webEnvironment = RANDOM_PORT)
public class ConsentDetailsApiControllerTest {
    private static final String BASE_URL = "http://localhost:";
    private static final String CONTEXT_DETAILS_URI = "/rcs/api/consent/details";
    private final Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
        @Override
        public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new DateTime(json.getAsString());
        }
    }).create();
    @LocalServerPort
    private int port;
    @MockBean
    private ConsentService consentService;
    @MockBean
    private AccountService accountService;
    @MockBean
    private ApiClientServiceClient apiClientService;
    @MockBean
    private UserServiceClient userServiceClient;
    @Autowired // needed for tests purposes
    private ConsentDetailsFactoryProvider consentDetailsFactoryProvider;
    @Autowired
    private TestRestTemplate restTemplate;

    private static Stream<Arguments> validPositiveArguments() {
        return Stream.of(
                arguments(
                        ACCOUNT_INTENT_ID,
                        aValidAccountConsentDetails(ACCOUNT_INTENT_ID),
                        IntentType.ACCOUNT_ACCESS_CONSENT,
                        AccountsConsentDetails.class
                ),
//                arguments(
//                        DOMESTIC_PAYMENT_INTENT_ID,
//                        aValidDomesticPaymentConsentDetails(DOMESTIC_PAYMENT_INTENT_ID),
//                        IntentType.PAYMENT_DOMESTIC_CONSENT,
//                        DomesticPaymentConsentDetails.class
//                ),
                arguments(
                        DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID,
                        aValidDomesticScheduledPaymentConsentDetails(DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID),
                        IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT,
                        DomesticScheduledPaymentConsentDetails.class
                ),
                arguments(
                        DOMESTIC_STANDING_ORDER_INTENT_ID,
                        aValidDomesticStandingOrderConsentDetails(DOMESTIC_STANDING_ORDER_INTENT_ID),
                        IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT,
                        DomesticStandingOrderConsentDetails.class
                ),
                arguments(
                        INTERNATIONAL_PAYMENT_INTENT_ID,
                        aValidInternationalPaymentConsentDetails(INTERNATIONAL_PAYMENT_INTENT_ID),
                        IntentType.PAYMENT_INTERNATIONAL_CONSENT,
                        InternationalPaymentConsentDetails.class
                ),
                arguments(
                        INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID,
                        aValidInternationalScheduledPaymentConsentDetails(INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID),
                        IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT,
                        InternationalScheduledPaymentConsentDetails.class
                ),
                arguments(
                        INTERNATIONAL_STANDING_ORDER_INTENT_ID,
                        aValidInternationalStandingOrderConsentDetails(INTERNATIONAL_STANDING_ORDER_INTENT_ID),
                        IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT,
                        InternationalStandingOrderConsentDetails.class
                ),
                arguments(
                        FILE_PAYMENT_INTENT_ID,
                        aValidFilePaymentConsentDetails(FILE_PAYMENT_INTENT_ID),
                        IntentType.PAYMENT_FILE_CONSENT,
                        FilePaymentConsentDetails.class
                )
        );
    }


    @ParameterizedTest
    @MethodSource("validPositiveArguments")
    public void shouldGetConsentDetails(
            String intentId,
            JsonObject consentDetails,
            IntentType intentType,
            Class<? extends PaymentsConsentDetails> classOf
    ) throws ExceptionClient {
        // given
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest(intentId);
        FRAccountWithBalance frAccountWithBalance = aValidFRAccountWithBalance();
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient(consentDetailsRequest.getClientId());
        given(apiClientService.getApiClient(anyString())).willReturn(apiClient);
        given(accountService.getAccountsWithBalance(anyString())).willReturn(List.of(frAccountWithBalance));
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentService.getConsent(any(ConsentClientDetailsRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(
                consentDetailsRequest.getClientId(),
                consentDetailsRequest.getIntentId(),
                consentDetailsRequest.getUser().getId()
        );
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(consentDetailURL, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);

        // Generic validations
        ConsentDetails responseDetails = gson.fromJson(response.getBody(), classOf);
        assertThat(responseDetails.getAccounts()).isNotEmpty();
        Assertions.assertThat(responseDetails.getIntentType()).isEqualTo(intentType);
        assertThat(responseDetails.getUserId()).isEqualTo(consentDetailsRequest.getUser().getId());
        assertThat(responseDetails.getUsername()).isEqualTo(consentDetailsRequest.getUser().getUserName());
        assertThat(responseDetails.getClientId()).isEqualTo(consentDetailsRequest.getClientId());
        assertThat(responseDetails.getLogo()).isEqualTo(apiClient.getLogoUri());

        // validations by payment
        validations(classOf, consentDetails, responseDetails);
    }

    private static Stream<Arguments> validNegativeArguments() {
        return Stream.of(
                arguments(
                        ACCOUNT_INTENT_ID,
                        aValidAccountConsentDetails(ACCOUNT_INTENT_ID)
                ),
//                arguments(
//                        DOMESTIC_PAYMENT_INTENT_ID,
//                        aValidDomesticPaymentConsentDetails(DOMESTIC_PAYMENT_INTENT_ID)
//                ),
                arguments(
                        DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID,
                        aValidDomesticScheduledPaymentConsentDetails(DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID)
                ),
                arguments(
                        DOMESTIC_STANDING_ORDER_INTENT_ID,
                        aValidDomesticStandingOrderConsentDetails(DOMESTIC_STANDING_ORDER_INTENT_ID)
                ),
                arguments(
                        INTERNATIONAL_PAYMENT_INTENT_ID,
                        aValidInternationalPaymentConsentDetails(INTERNATIONAL_PAYMENT_INTENT_ID)
                ),
                arguments(
                        INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID,
                        aValidInternationalScheduledPaymentConsentDetails(INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID)
                ),
                arguments(
                        INTERNATIONAL_STANDING_ORDER_INTENT_ID,
                        aValidInternationalStandingOrderConsentDetails(INTERNATIONAL_STANDING_ORDER_INTENT_ID)
                ),
                arguments(
                        FILE_PAYMENT_INTENT_ID,
                        aValidFilePaymentConsentDetails(FILE_PAYMENT_INTENT_ID)
                )
                ,
                arguments(
                        DOMESTIC_VRP_PAYMENT_INTENT_ID,
                        aValidDomesticVrpPaymentConsentDetails(DOMESTIC_VRP_PAYMENT_INTENT_ID)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("validNegativeArguments")
    public void shouldGetRedirectActionWhenUserNotFound(
            String intentId,
            JsonObject consentDetails
    ) throws ExceptionClient {
        // given
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest(intentId);
        FRAccountWithBalance frAccountWithBalance = aValidFRAccountWithBalance();
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient(consentDetailsRequest.getClientId());
        given(apiClientService.getApiClient(anyString())).willReturn(apiClient);
        given(accountService.getAccountsWithBalance(anyString())).willReturn(List.of(frAccountWithBalance));
        String message = String.format("User data with userId '%s' not found.", user.getId());
        ExceptionClient exceptionClient = new ExceptionClient(
                ErrorClient.builder()
                        .errorType(ErrorType.NOT_FOUND)
                        .userId(user.getId())
                        .build(),
                message);
        given(userServiceClient.getUser(anyString())).willThrow(exceptionClient);
        given(consentService.getConsent(any(ConsentClientDetailsRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(
                consentDetailsRequest.getClientId(),
                consentDetailsRequest.getIntentId(),
                consentDetailsRequest.getUser().getId()
        );
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(Objects.requireNonNull(response.getBody()).getRedirectUri()).isNotEmpty();
        assertThat(Objects.requireNonNull(response.getBody().getConsentJwt())).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("validNegativeArguments")
    public void shouldGetRedirectActionWhenApiClientNotFound(
            String intentId,
            JsonObject consentDetails
    ) throws ExceptionClient {
        // given
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest(intentId);
        FRAccountWithBalance frAccountWithBalance = aValidFRAccountWithBalance();
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        String message = String.format("ClientId '%s' not found.", consentDetailsRequest.getClientId());

        ExceptionClient exceptionClient = new ExceptionClient(
                ErrorClient.builder()
                        .errorType(ErrorType.NOT_FOUND)
                        .clientId(consentDetailsRequest.getClientId())
                        .build(),
                message
        );
        given(apiClientService.getApiClient(anyString())).willThrow(exceptionClient);

        given(accountService.getAccountsWithBalance(anyString())).willReturn(List.of(frAccountWithBalance));

        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentService.getConsent(any(ConsentClientDetailsRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("validNegativeArguments")
    public void shouldGetRedirectActionWhenConsentNotFound(String intentId) throws ExceptionClient {
        // given
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest(intentId);
        FRAccountWithBalance frAccountWithBalance = aValidFRAccountWithBalance();
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient(consentDetailsRequest.getClientId());
        given(apiClientService.getApiClient(anyString())).willReturn(apiClient);
        given(accountService.getAccountsWithBalance(anyString())).willReturn(List.of(frAccountWithBalance));
        given(userServiceClient.getUser(anyString())).willReturn(user);

        String message = String.format("The AISP '%s' is referencing an account consent detailsRequest '%s' " +
                "that doesn't exist", consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId());
        ExceptionClient exceptionClient = new ExceptionClient(consentDetailsRequest, ErrorType.NOT_FOUND, message);
        given(consentService.getConsent(any(ConsentClientDetailsRequest.class))).willThrow(exceptionClient);

        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(
                consentDetailsRequest.getClientId(),
                consentDetailsRequest.getIntentId(),
                consentDetailsRequest.getUser().getId()
        );
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldGetDomesticVrpPaymentConsentDetailsSweeping() throws ExceptionClient {
        // given
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest(IntentType.DOMESTIC_VRP_PAYMENT_CONSENT);
        FRAccountWithBalance frAccountWithBalance = aValidFRAccountWithBalance();
        JsonObject consentDetails = aValidDomesticVrpPaymentConsentDetails(
                consentDetailsRequest.getIntentId(), frAccountWithBalance.getAccount().getFirstAccount()
        );

        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient(consentDetailsRequest.getClientId());
        given(apiClientService.getApiClient(anyString())).willReturn(apiClient);
        given(accountService.getAccountWithBalanceByIdentifiers(anyString(), anyString(), anyString(), anyString())).willReturn(
                frAccountWithBalance
        );
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentService.getConsent(any(ConsentClientDetailsRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(
                consentDetailsRequest.getClientId(),
                consentDetailsRequest.getIntentId(),
                consentDetailsRequest.getUser().getId()
        );
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(consentDetailURL, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);

        DomesticVrpPaymentConsentDetails responseDetails = gson.fromJson(response.getBody(), DomesticVrpPaymentConsentDetails.class);
        assertThat(responseDetails.getIntentType()).isEqualTo(IntentType.DOMESTIC_VRP_PAYMENT_CONSENT);
        assertThat(responseDetails.getAccounts()).isNotEmpty();
        // We expect only one account (the debtor account) because the debtor account is provided in the consent for sweeping case
        assertThat(responseDetails.getAccounts()).hasSize(1);
        assertThat(responseDetails.getUserId()).isEqualTo(consentDetailsRequest.getUser().getId());
        assertThat(responseDetails.getUsername()).isEqualTo(consentDetailsRequest.getUser().getUserName());
        assertThat(responseDetails.getClientId()).isEqualTo(consentDetailsRequest.getClientId());
        assertThat(responseDetails.getLogo()).isEqualTo(apiClient.getLogoUri());
        FRAccountIdentifier accountIdentifier = frAccountWithBalance.getAccount().getFirstAccount();
        FRAccountIdentifier debtorAccount = responseDetails.getInitiation().getDebtorAccount();
        assertThat(debtorAccount.getAccountId()).isEqualTo(frAccountWithBalance.getAccount().getAccountId());
        assertThat(debtorAccount.getIdentification()).isEqualTo(accountIdentifier.getIdentification());
        assertThat(debtorAccount.getName()).isEqualTo(accountIdentifier.getName());
        assertThat(debtorAccount.getSchemeName()).isEqualTo(accountIdentifier.getSchemeName());
    }

    private void validations(
            Class classOf,
            JsonObject consentDetails,
            ConsentDetails responseDetails
    ) {
        switch (classOf.getSimpleName()) {
            case "DomesticScheduledPaymentConsentDetails":
                validateDomesticScheduledConsentDetailsResponse(
                        consentDetails,
                        (DomesticScheduledPaymentConsentDetails) responseDetails
                );
                break;
            case "DomesticStandingOrderConsentDetails":
                validateDomesticStandingOrderConsentDetailsResponse(
                        consentDetails,
                        (DomesticStandingOrderConsentDetails) responseDetails
                );
                break;
        }
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        headers.add("Cookie", "iPlanetDirectoryPro=aSsoToken");
        return headers;
    }
}
