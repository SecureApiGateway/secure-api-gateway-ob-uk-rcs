/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details;

import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.ConsentDetailsRequestTestDataFactory.aValidConsentDetailsRequest;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.UserTestDataFactory.aValidUser;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriUtils;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.UserServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.RCSServerApplicationTestSupport;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.testsupport.JwtTestHelper;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreEnabledIntentTypes;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

/**
 * Tests for the {@link ConsentDetailsApiController} when it is configured to use the RCS Consent Store module.
 */
@ActiveProfiles("test")
@SpringBootTest(classes = RCSServerApplicationTestSupport.class, webEnvironment = RANDOM_PORT)
public class ConsentDetailsApiControllerRcsConsentStoreTest {

    public static final String TPP_LOGO = "tppLogo";
    @LocalServerPort
    private int port;

    private String consentDetailsUri;

    @BeforeEach
    public void beforeEach() {
        consentDetailsUri =  "http://localhost:" + port + "/rcs/api/consent/details";
    }

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private ConsentStoreDetailsServiceRegistry consentStoreDetailsServiceRegistry;

    @Autowired
    private ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes;

    @Autowired
    private TestRestTemplate restTemplate;

    private static DomesticPaymentConsentDetails createDomesticPaymentConsentDetails() {
        final DomesticPaymentConsentDetails domesticPaymentConsentDetails = new DomesticPaymentConsentDetails();
        domesticPaymentConsentDetails.setConsentId(IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId());
        domesticPaymentConsentDetails.setLogo(TPP_LOGO);
        return domesticPaymentConsentDetails;
    }

    private static DomesticScheduledPaymentConsentDetails createDomesticScheduledPaymentConsentDetails() {
        final DomesticScheduledPaymentConsentDetails domesticScheduled = new DomesticScheduledPaymentConsentDetails();
        domesticScheduled.setConsentId(IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT.generateIntentId());
        domesticScheduled.setLogo(TPP_LOGO);
        return domesticScheduled;
    }

    private static DomesticStandingOrderConsentDetails createDomesticStandingOrderConsentDetails() {
        final DomesticStandingOrderConsentDetails domesticStandingOrder = new DomesticStandingOrderConsentDetails();
        domesticStandingOrder.setConsentId(IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT.generateIntentId());
        domesticStandingOrder.setLogo(TPP_LOGO);
        return domesticStandingOrder;
    }

    private static InternationalPaymentConsentDetails createInternationalPaymentConsentDetails() {
        final InternationalPaymentConsentDetails internationalPaymentConsentDetails = new InternationalPaymentConsentDetails();
        internationalPaymentConsentDetails.setConsentId(IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId());
        internationalPaymentConsentDetails.setLogo(TPP_LOGO);
        return internationalPaymentConsentDetails;
    }

    private static InternationalScheduledPaymentConsentDetails createInternationalScheduledPaymentConsentDetails() {
        final InternationalScheduledPaymentConsentDetails consentDetails = new InternationalScheduledPaymentConsentDetails();
        consentDetails.setConsentId(IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT.generateIntentId());
        consentDetails.setLogo(TPP_LOGO);
        return consentDetails;
    }

    private static InternationalStandingOrderConsentDetails createInternationalStandingOrderConsentDetails() {
        final InternationalStandingOrderConsentDetails consentDetails = new InternationalStandingOrderConsentDetails();
        consentDetails.setConsentId(IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT.generateIntentId());
        consentDetails.setLogo(TPP_LOGO);
        return consentDetails;
    }

    private static FilePaymentConsentDetails createFilePaymentConsentDetails() {
        final FilePaymentConsentDetails filePaymentConsentDetails = new FilePaymentConsentDetails();
        filePaymentConsentDetails.setConsentId(IntentType.PAYMENT_FILE_CONSENT.generateIntentId());
        filePaymentConsentDetails.setLogo(TPP_LOGO);
        return filePaymentConsentDetails;
    }

    private static DomesticVrpPaymentConsentDetails createVrpConsentDetails() {
        final DomesticVrpPaymentConsentDetails consentDetails = new DomesticVrpPaymentConsentDetails();
        consentDetails.setConsentId(IntentType.DOMESTIC_VRP_PAYMENT_CONSENT.generateIntentId());
        consentDetails.setLogo(TPP_LOGO);
        return consentDetails;
    }

    private static Stream<Arguments> validConsentDetailsArguments() {
        return Stream.of(
                arguments(IntentType.PAYMENT_DOMESTIC_CONSENT, createDomesticPaymentConsentDetails(), DomesticPaymentConsentDetails.class),
                arguments(IntentType.ACCOUNT_ACCESS_CONSENT, createAccountAccessConsentDetails(), AccountsConsentDetails.class),
                arguments(IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT, createDomesticScheduledPaymentConsentDetails(), DomesticScheduledPaymentConsentDetails.class),
                arguments(IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT, createDomesticStandingOrderConsentDetails(), DomesticStandingOrderConsentDetails.class),
                arguments(IntentType.PAYMENT_INTERNATIONAL_CONSENT, createInternationalPaymentConsentDetails(), InternationalPaymentConsentDetails.class),
                arguments(IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT, createInternationalScheduledPaymentConsentDetails(), InternationalScheduledPaymentConsentDetails.class),
                arguments(IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT, createInternationalStandingOrderConsentDetails(), InternationalStandingOrderConsentDetails.class),
                arguments(IntentType.PAYMENT_FILE_CONSENT, createFilePaymentConsentDetails(), FilePaymentConsentDetails.class),
                arguments(IntentType.DOMESTIC_VRP_PAYMENT_CONSENT, createVrpConsentDetails(), DomesticVrpPaymentConsentDetails.class),
                arguments(IntentType.CUSTOMER_INFO_CONSENT, createCustomerInfoConsentDetails(), CustomerInfoConsentDetails.class)
        );
    }

    private static AccountsConsentDetails createAccountAccessConsentDetails() {
        final AccountsConsentDetails accountsConsentDetails = new AccountsConsentDetails();
        accountsConsentDetails.setConsentId(IntentType.ACCOUNT_ACCESS_CONSENT.generateIntentId());
        accountsConsentDetails.setLogo(TPP_LOGO);
        return accountsConsentDetails;
    }

    private static CustomerInfoConsentDetails createCustomerInfoConsentDetails() {
        final CustomerInfoConsentDetails customerInfoConsentDetails = new CustomerInfoConsentDetails();
        customerInfoConsentDetails.setConsentId(IntentType.CUSTOMER_INFO_CONSENT.generateIntentId());
        customerInfoConsentDetails.setLogo(TPP_LOGO);
        return customerInfoConsentDetails;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validConsentDetailsArguments")
    public <C extends ConsentDetails> void shouldGetParameterizedConsentDetails(IntentType intentType, ConsentDetails testConsentDetails,
                                                                                  Class<C> consentDetailsClass) throws Exception {

        final String consentId = testConsentDetails.getConsentId();
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest(consentId);
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentStoreDetailsServiceRegistry.isIntentTypeSupported(eq(intentType))).willReturn(Boolean.TRUE);

        final ArgumentCaptor<ConsentClientDetailsRequest> consentDetailsArgCaptor = ArgumentCaptor.forClass(ConsentClientDetailsRequest.class);
        given(consentStoreDetailsServiceRegistry.getDetailsFromConsentStore(eq(intentType), consentDetailsArgCaptor.capture())).willReturn(testConsentDetails);

        String jwtRequest = JwtTestHelper.consentRequestJwt(
                consentDetailsRequest.getClientId(),
                consentDetailsRequest.getIntentId(),
                consentDetailsRequest.getUser().getId()
        );
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        // when
        ResponseEntity<C> response = restTemplate.postForEntity(consentDetailsUri, request, consentDetailsClass);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final ConsentClientDetailsRequest capturedConsentClientDetailsRequest = consentDetailsArgCaptor.getValue();
        assertThat(capturedConsentClientDetailsRequest.getClientId()).isEqualTo(consentDetailsRequest.getClientId());
        assertThat(capturedConsentClientDetailsRequest.getUser()).isEqualTo(user);
        assertThat(capturedConsentClientDetailsRequest.getIntentId()).isEqualTo(consentDetailsRequest.getIntentId());
        assertThat(capturedConsentClientDetailsRequest.getConsentRequestJwt()).isNotNull();

        final C consentDetailsResponse = response.getBody();
        assertThat(consentDetailsResponse).isEqualTo(testConsentDetails);
    }

    @Test
    public void shouldGetRedirectActionWhenUserNotFound() throws ExceptionClient {
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest("intent-12345454");
        User user = aValidUser();
        consentDetailsRequest.setUser(user);

        String message = String.format("User data with userId '%s' not found.", user.getId());
        ExceptionClient exceptionClient = new ExceptionClient(
                ErrorClient.builder()
                        .errorType(ErrorType.NOT_FOUND)
                        .userId(user.getId())
                        .build(),
                message);
        given(userServiceClient.getUser(anyString())).willThrow(exceptionClient);


        String jwtRequest = JwtTestHelper.consentRequestJwt(
                consentDetailsRequest.getClientId(),
                consentDetailsRequest.getIntentId(),
                consentDetailsRequest.getUser().getId()
        );
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailsUri, request, RedirectionAction.class);

        assertThat(Objects.requireNonNull(response.getBody()).getRedirectUri()).isNotEmpty();
        assertThat(Objects.requireNonNull(response.getBody().getConsentJwt())).isNotEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldGetRedirectActionWhenConsentNotFound() throws ExceptionClient {
        final IntentType intentType = IntentType.PAYMENT_DOMESTIC_CONSENT;
        final String consentId = intentType.generateIntentId();
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest(consentId);
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentStoreDetailsServiceRegistry.isIntentTypeSupported(eq(intentType))).willReturn(Boolean.TRUE);

        final ArgumentCaptor<ConsentClientDetailsRequest> consentDetailsArgCaptor = ArgumentCaptor.forClass(ConsentClientDetailsRequest.class);
        given(consentStoreDetailsServiceRegistry.getDetailsFromConsentStore(eq(intentType), consentDetailsArgCaptor.capture())).willThrow(
                new ConsentStoreException(ConsentStoreException.ErrorType.NOT_FOUND, consentId));

        String jwtRequest = JwtTestHelper.consentRequestJwt(
                consentDetailsRequest.getClientId(),
                consentDetailsRequest.getIntentId(),
                consentDetailsRequest.getUser().getId()
        );
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailsUri, request, RedirectionAction.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        final RedirectionAction redirectionAction = response.getBody();
        assertThat(redirectionAction.getErrorMessage()).isEqualTo("Object requested not found");
        assertThat(Objects.requireNonNull(redirectionAction).getRedirectUri()).isNotEmpty();
        assertThat(Objects.requireNonNull(redirectionAction.getConsentJwt())).isNotEmpty();

    }

    @Test
    public void shouldGetRedirectActionWhenConsentInvalidPermissions() throws ExceptionClient {
        final IntentType intentType = IntentType.PAYMENT_DOMESTIC_CONSENT;
        final String consentId = intentType.generateIntentId();
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest(consentId);
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentStoreDetailsServiceRegistry.isIntentTypeSupported(eq(intentType))).willReturn(Boolean.TRUE);

        final ArgumentCaptor<ConsentClientDetailsRequest> consentDetailsArgCaptor = ArgumentCaptor.forClass(ConsentClientDetailsRequest.class);
        given(consentStoreDetailsServiceRegistry.getDetailsFromConsentStore(eq(intentType), consentDetailsArgCaptor.capture())).willThrow(
                new ConsentStoreException(ConsentStoreException.ErrorType.INVALID_PERMISSIONS, consentId));

        String jwtRequest = JwtTestHelper.consentRequestJwt(
                consentDetailsRequest.getClientId(),
                consentDetailsRequest.getIntentId(),
                consentDetailsRequest.getUser().getId()
        );
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailsUri, request, RedirectionAction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        final RedirectionAction redirectionAction = response.getBody();
        assertThat(redirectionAction.getErrorMessage()).isEqualTo("The resource owner or authorization server denied the request.");
        assertThat(Objects.requireNonNull(redirectionAction).getRedirectUri()).isNotEmpty();
        assertThat(Objects.requireNonNull(redirectionAction.getConsentJwt())).isNotEmpty();
    }

    @Test
    void shouldGetRedirectActionWhenConsentReAuthenticationNotSupported() throws ExceptionClient {
        final IntentType intentType = IntentType.PAYMENT_DOMESTIC_CONSENT;
        final String consentId = intentType.generateIntentId();
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest(consentId);
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentStoreDetailsServiceRegistry.isIntentTypeSupported(eq(intentType))).willReturn(Boolean.TRUE);

        final ArgumentCaptor<ConsentClientDetailsRequest> consentDetailsArgCaptor = ArgumentCaptor.forClass(ConsentClientDetailsRequest.class);
        given(consentStoreDetailsServiceRegistry.getDetailsFromConsentStore(eq(intentType), consentDetailsArgCaptor.capture())).willThrow(
                new ConsentStoreException(ConsentStoreException.ErrorType.CONSENT_REAUTHENTICATION_NOT_SUPPORTED, consentId));

        String jwtRequest = JwtTestHelper.consentRequestJwt(
                consentDetailsRequest.getClientId(),
                consentDetailsRequest.getIntentId(),
                consentDetailsRequest.getUser().getId()
        );
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailsUri, request, RedirectionAction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        final RedirectionAction redirectionAction = response.getBody();
        assertThat(redirectionAction.getErrorMessage()).isEqualTo("The resource owner or authorization server denied the request.");
        assertThat(redirectionAction.getRedirectUri()).isNotEmpty();
        assertThat(redirectionAction.getConsentJwt()).isNotEmpty();
    }

    @Test
    public void shouldGetRedirectActionWhenNotPermissionedToAccessDebtorAccount() throws ExceptionClient {
        final IntentType intentType = IntentType.PAYMENT_DOMESTIC_CONSENT;
        final String consentId = intentType.generateIntentId();
        ConsentClientDetailsRequest consentDetailsRequest = aValidConsentDetailsRequest(consentId);
        User user = aValidUser();
        consentDetailsRequest.setUser(user);
        given(userServiceClient.getUser(anyString())).willReturn(user);
        given(consentStoreDetailsServiceRegistry.isIntentTypeSupported(eq(intentType))).willReturn(Boolean.TRUE);

        final ArgumentCaptor<ConsentClientDetailsRequest> consentDetailsArgCaptor = ArgumentCaptor.forClass(ConsentClientDetailsRequest.class);
        given(consentStoreDetailsServiceRegistry.getDetailsFromConsentStore(eq(intentType), consentDetailsArgCaptor.capture())).willThrow(
                new ConsentStoreException(ConsentStoreException.ErrorType.INVALID_DEBTOR_ACCOUNT, consentId));

        String jwtRequest = JwtTestHelper.consentRequestJwt(
                consentDetailsRequest.getClientId(),
                consentDetailsRequest.getIntentId(),
                consentDetailsRequest.getUser().getId()
        );
        HttpEntity<String> request = new HttpEntity<>(jwtRequest, headers());

        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailsUri, request, RedirectionAction.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        final RedirectionAction redirectionAction = response.getBody();
        assertThat(redirectionAction.getErrorMessage()).isEqualTo("The resource owner or authorization server denied the request.");
        assertThat(redirectionAction.getRedirectUri()).isNotEmpty()
                .contains(UriUtils.encode("User is not permissioned to make a payment from the debtorAccount specified", StandardCharsets.UTF_8));
        assertThat(Objects.requireNonNull(redirectionAction).getRedirectUri()).isNotEmpty();
        assertThat(Objects.requireNonNull(redirectionAction.getConsentJwt())).isNotEmpty();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }
}
