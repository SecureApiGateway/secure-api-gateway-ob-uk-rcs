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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.forgerock.FRFrequency;
import com.forgerock.securebanking.openbanking.uk.rcs.RcsApplicationTestSupport;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.*;
import com.forgerock.securebanking.openbanking.uk.rcs.client.rs.AccountService;
import com.forgerock.securebanking.openbanking.uk.rcs.factory.ConsentDetailsFactoryProvider;
import com.forgerock.securebanking.openbanking.uk.rcs.testsupport.JwtTestHelper;
import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.ApiClient;
import com.forgerock.securebanking.platform.client.models.ConsentRequest;
import com.forgerock.securebanking.platform.client.models.User;
import com.forgerock.securebanking.platform.client.services.ApiClientServiceClient;
import com.forgerock.securebanking.platform.client.services.ConsentService;
import com.forgerock.securebanking.platform.client.services.UserServiceClient;
import com.forgerock.securebanking.platform.client.test.support.ApiClientTestDataFactory;
import com.google.gson.*;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.junit.jupiter.api.Test;
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

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.ConsentDetailsRequestTestDataFactory.*;
import static com.forgerock.securebanking.platform.client.test.support.DomesticPaymentConsentDetailsTestFactory.aValidDomesticPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.DomesticScheduledPaymentConsentDetailsTestFactory.aValidDomesticScheduledPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.DomesticStandingOrderConsentDetailsTestFactory.aValidDomesticStandingOrderConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory.aValidDomesticVrpPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.UserTestDataFactory.aValidUser;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Spring Boot Test for {@link ConsentDetailsApiController}.
 */
// TODO: first approach to tests the controller
@EnableConfigurationProperties
@ActiveProfiles("test")
@SpringBootTest(classes = RcsApplicationTestSupport.class, webEnvironment = RANDOM_PORT)
public class ConsentDetailsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String CONTEXT_DETAILS_URI = "/api/rcs/consent/details";
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
    private ConsentDetailsFactoryProvider consentDetailsFactoryLocator;
    @Autowired
    private TestRestTemplate restTemplate;

    // ACCOUNT
    @Test
    public void ShouldGetAccountConsentDetails() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidAccountConsentDetailsRequest();
        JsonObject consentDetails = aValidAccountConsentDetails(consentDetailsRequest.getIntentId());
        FRAccountWithBalance frAccountWithBalance = aValidFRAccountWithBalance();
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient(consentDetailsRequest.getClientId());
        given(apiClientService.getApiClient(anyString())).willReturn(apiClient);
        given(accountService.getAccountsWithBalance(anyString())).willReturn(List.of(frAccountWithBalance));
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(consentDetailURL, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);

        AccountsConsentDetails responseBody = gson.fromJson(response.getBody(), AccountsConsentDetails.class);
        assertThat(responseBody.getAccounts()).isNotEmpty();
        assertThat(responseBody.getIntentType()).isEqualTo(IntentType.ACCOUNT_ACCESS_CONSENT);
        assertThat(responseBody.getUserId()).isEqualTo(consentDetailsRequest.getUser().getId());
        assertThat(responseBody.getUsername()).isEqualTo(consentDetailsRequest.getUser().getUserName());
        assertThat(responseBody.getClientId()).isEqualTo(consentDetailsRequest.getClientId());
        assertThat(responseBody.getLogo()).isEqualTo(apiClient.getLogoUri());
    }

    @Test
    public void ShouldGetRedirectActionWhenUserNotFoundAccounts() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidAccountConsentDetailsRequest();
        JsonObject consentDetails = aValidAccountConsentDetails(consentDetailsRequest.getIntentId());
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
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(Objects.requireNonNull(response.getBody()).getRedirectUri()).isNotEmpty();
        assertThat(Objects.requireNonNull(response.getBody().getConsentJwt())).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void ShouldGetRedirectActionWhenApiClientNotFoundAccounts() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidAccountConsentDetailsRequest();
        JsonObject consentDetails = aValidAccountConsentDetails(consentDetailsRequest.getIntentId());
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
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void ShouldGetRedirectActionWhenConsentNotFoundAccounts() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidAccountConsentDetailsRequest();
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
        given(consentService.getConsent(any(ConsentRequest.class))).willThrow(exceptionClient);

        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // DOMESTIC PAYMENT
    @Test
    public void ShouldGetDomesticPaymentConsentDetails() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticPaymentConsentDetailsRequest();
        JsonObject consentDetails = aValidDomesticPaymentConsentDetails(consentDetailsRequest.getIntentId());
        FRAccountWithBalance frAccountWithBalance = aValidFRAccountWithBalance();
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient(consentDetailsRequest.getClientId());
        given(apiClientService.getApiClient(anyString())).willReturn(apiClient);
        given(accountService.getAccountsWithBalance(anyString())).willReturn(List.of(frAccountWithBalance));
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(consentDetailURL, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);

        DomesticPaymentConsentDetails responseBody = gson.fromJson(response.getBody(), DomesticPaymentConsentDetails.class);
        assertThat(responseBody.getAccounts()).isNotEmpty();
        assertThat(responseBody.getIntentType()).isEqualTo(IntentType.PAYMENT_DOMESTIC_CONSENT);
        assertThat(responseBody.getUserId()).isEqualTo(consentDetailsRequest.getUser().getId());
        assertThat(responseBody.getUsername()).isEqualTo(consentDetailsRequest.getUser().getUserName());
        assertThat(responseBody.getClientId()).isEqualTo(consentDetailsRequest.getClientId());
        assertThat(responseBody.getLogo()).isEqualTo(apiClient.getLogoUri());
    }

    @Test
    public void ShouldGetRedirectActionWhenConsentNotFoundDomesticPayments() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticPaymentConsentDetailsRequest();
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
        given(consentService.getConsent(any(ConsentRequest.class))).willThrow(exceptionClient);

        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void ShouldGetRedirectActionWhenUserNotFoundDomesticPayments() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticPaymentConsentDetailsRequest();
        JsonObject consentDetails = aValidDomesticPaymentConsentDetails(consentDetailsRequest.getIntentId());
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
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    public void ShouldGetRedirectActionWhenApiClientNotFoundDomesticPayments() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticPaymentConsentDetailsRequest();
        JsonObject consentDetails = aValidDomesticPaymentConsentDetails(consentDetailsRequest.getIntentId());
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
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // DOMESTIC SCHEDULED PAYMENT
    @Test
    public void ShouldGetDomesticScheduledPaymentConsentDetails() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticScheduledPaymentConsentDetailsRequest();
        JsonObject consentDetails = aValidDomesticScheduledPaymentConsentDetails(consentDetailsRequest.getIntentId());
        FRAccountWithBalance frAccountWithBalance = aValidFRAccountWithBalance();
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient(consentDetailsRequest.getClientId());
        given(apiClientService.getApiClient(anyString())).willReturn(apiClient);
        given(accountService.getAccountsWithBalance(anyString())).willReturn(List.of(frAccountWithBalance));
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(consentDetailURL, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);

        DomesticScheduledPaymentConsentDetails responseBody = gson.fromJson(response.getBody(), DomesticScheduledPaymentConsentDetails.class);
        assertThat(responseBody.getAccounts()).isNotEmpty();
        assertThat(responseBody.getIntentType()).isEqualTo(IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT);
        assertThat(responseBody.getUserId()).isEqualTo(consentDetailsRequest.getUser().getId());
        assertThat(responseBody.getUsername()).isEqualTo(consentDetailsRequest.getUser().getUserName());
        assertThat(responseBody.getClientId()).isEqualTo(consentDetailsRequest.getClientId());
        assertThat(responseBody.getLogo()).isEqualTo(apiClient.getLogoUri());
        assertThat(responseBody.getPaymentDate()
                .isEqual(
                        Instant.parse(
                                consentDetails.getAsJsonObject(OB_INTENT_OBJECT)
                                        .getAsJsonObject(DATA)
                                        .getAsJsonObject(INITIATION)
                                        .get(REQUESTED_EXECUTION_DATETIME).getAsString()
                        ).toDateTime()
                )
        );
    }

    @Test
    public void ShouldGetRedirectActionWhenConsentNotFoundDomesticScheduledPayments() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticScheduledPaymentConsentDetailsRequest();
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
        given(consentService.getConsent(any(ConsentRequest.class))).willThrow(exceptionClient);

        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void ShouldGetRedirectActionWhenUserNotFoundDomesticScheduledPayments() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticScheduledPaymentConsentDetailsRequest();
        JsonObject consentDetails = aValidDomesticScheduledPaymentConsentDetails(consentDetailsRequest.getIntentId());
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
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    public void ShouldGetRedirectActionWhenApiClientNotFoundDomesticScheduledPayments() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticScheduledPaymentConsentDetailsRequest();
        JsonObject consentDetails = aValidDomesticScheduledPaymentConsentDetails(consentDetailsRequest.getIntentId());
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
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // DOMESTIC STANDING ORDER PAYMENT
    @Test
    public void ShouldGetDomesticStandingOrderConsentDetails() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticStandingOrderConsentDetailsRequest();
        JsonObject consentDetails = aValidDomesticStandingOrderConsentDetails(consentDetailsRequest.getIntentId());
        FRAccountWithBalance frAccountWithBalance = aValidFRAccountWithBalance();
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient(consentDetailsRequest.getClientId());
        given(apiClientService.getApiClient(anyString())).willReturn(apiClient);
        given(accountService.getAccountsWithBalance(anyString())).willReturn(List.of(frAccountWithBalance));
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;
        String jwtRequest = JwtTestHelper.consentRequestJwt(consentDetailsRequest.getClientId(), consentDetailsRequest.getIntentId(), consentDetailsRequest.getUser().getId());
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(consentDetailURL, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);

        DomesticStandingOrderConsentDetails responseBody = gson.fromJson(response.getBody(), DomesticStandingOrderConsentDetails.class);
        assertThat(responseBody.getAccounts()).isNotEmpty();
        assertThat(responseBody.getIntentType()).isEqualTo(IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT);
        assertThat(responseBody.getUserId()).isEqualTo(consentDetailsRequest.getUser().getId());
        assertThat(responseBody.getUsername()).isEqualTo(consentDetailsRequest.getUser().getUserName());
        assertThat(responseBody.getClientId()).isEqualTo(consentDetailsRequest.getClientId());
        assertThat(responseBody.getLogo()).isEqualTo(apiClient.getLogoUri());

        final JsonObject expectedIntentData = consentDetails.getAsJsonObject(OB_INTENT_OBJECT).getAsJsonObject(DATA);
        final JsonObject initiation = expectedIntentData.getAsJsonObject(INITIATION);

        assertThat(responseBody.getStandingOrder().getFinalPaymentDateTime()
                .isEqual(Instant.parse(initiation.get(FINAL_PAYMENT_DATETIME).getAsString()).toDateTime())
        );
        assertThat(responseBody.getStandingOrder().getFirstPaymentDateTime()
                .isEqual(Instant.parse(initiation.get(FIRST_PAYMENT_DATETIME).getAsString()).toDateTime())
        );

        assertThat(responseBody.getStandingOrder().getRecurringPaymentDateTime()
                .isEqual(Instant.parse(initiation.get(RECURRING_PAYMENT_DATETIME).getAsString()).toDateTime())
        );

        assertThat(responseBody.getStandingOrder().getFrequency())
                .isEqualTo((new FRFrequency(initiation.get(FREQUENCY).getAsString())).getSentence());

        assertThat(responseBody.getStandingOrder().getFinalPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(FINAL_PAYMENT_AMOUNT).get(AMOUNT).getAsString());
        assertThat(responseBody.getStandingOrder().getFinalPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(FINAL_PAYMENT_AMOUNT).get(CURRENCY).getAsString());

        assertThat(responseBody.getStandingOrder().getFirstPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(FIRST_PAYMENT_AMOUNT).get(AMOUNT).getAsString());
        assertThat(responseBody.getStandingOrder().getFirstPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(FIRST_PAYMENT_AMOUNT).get(CURRENCY).getAsString());

        assertThat(responseBody.getStandingOrder().getRecurringPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(RECURRING_PAYMENT_AMOUNT).get(AMOUNT).getAsString());
        assertThat(responseBody.getStandingOrder().getRecurringPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(RECURRING_PAYMENT_AMOUNT).get(CURRENCY).getAsString());
    }

    @Test
    public void ShouldGetRedirectActionWhenConsentNotFoundDomesticStandingOrder() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticStandingOrderConsentDetailsRequest();
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
        given(consentService.getConsent(any(ConsentRequest.class))).willThrow(exceptionClient);

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
    public void ShouldGetRedirectActionWhenUserNotFoundDomesticStandingOrder() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticStandingOrderConsentDetailsRequest();
        JsonObject consentDetails = aValidDomesticStandingOrderConsentDetails(consentDetailsRequest.getIntentId());
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
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
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
    public void ShouldGetRedirectActionWhenApiClientNotFoundDomesticStandingOrder() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticStandingOrderConsentDetailsRequest();
        JsonObject consentDetails = aValidDomesticStandingOrderConsentDetails(consentDetailsRequest.getIntentId());
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
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
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

    // DOMESTIC VRP PAYMENT
    @Test
    public void ShouldGetDomesticVrpPaymentConsentDetailsSweeping() throws ExceptionClient {
        // given
        ConsentRequest consentDetailsRequest = aValidDomesticVrpPaymentConsentDetailsRequest();
        JsonObject consentDetails = aValidDomesticVrpPaymentConsentDetails(consentDetailsRequest.getIntentId());
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient(consentDetailsRequest.getClientId());
        given(apiClientService.getApiClient(anyString())).willReturn(apiClient);
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentService.getConsent(any(ConsentRequest.class))).willReturn(consentDetails);
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

        DomesticVrpPaymentConsentDetails responseBody = gson.fromJson(response.getBody(), DomesticVrpPaymentConsentDetails.class);
        assertThat(responseBody.getIntentType()).isEqualTo(IntentType.DOMESTIC_VRP_PAYMENT_CONSENT);
        assertThat(responseBody.getUserId()).isEqualTo(consentDetailsRequest.getUser().getId());
        assertThat(responseBody.getUsername()).isEqualTo(consentDetailsRequest.getUser().getUserName());
        assertThat(responseBody.getClientId()).isEqualTo(consentDetailsRequest.getClientId());
        assertThat(responseBody.getLogo()).isEqualTo(apiClient.getLogoUri());
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        headers.add("Cookie", "iPlanetDirectoryPro=aSsoToken");
        return headers;
    }

}
