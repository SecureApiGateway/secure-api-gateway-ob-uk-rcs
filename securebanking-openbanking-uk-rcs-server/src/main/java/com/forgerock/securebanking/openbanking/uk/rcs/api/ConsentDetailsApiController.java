/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
import com.forgerock.securebanking.openbanking.uk.common.api.meta.IntentType;
import com.forgerock.securebanking.openbanking.uk.common.claim.Claims;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.rs.AccountService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.rs.DomesticPaymentService;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.general.ConsentDetailsBuilderFactory;
import com.forgerock.securebanking.openbanking.uk.rcs.exception.InvalidConsentException;
import com.forgerock.securebanking.platform.client.Constants;
import com.forgerock.securebanking.platform.client.configuration.ConfigurationPropertiesClient;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentRequest;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentRequest;
import com.forgerock.securebanking.platform.client.models.general.ApiClient;
import com.forgerock.securebanking.platform.client.models.general.Consent;
import com.forgerock.securebanking.platform.client.models.general.ConsentDecision;
import com.forgerock.securebanking.platform.client.models.general.User;
import com.forgerock.securebanking.platform.client.services.general.ApiClientServiceClient;
import com.forgerock.securebanking.platform.client.services.general.ConsentServiceClient;
import com.forgerock.securebanking.platform.client.services.general.UserServiceClient;
import com.forgerock.securebanking.platform.client.utils.jwt.JwtUtil;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;

import static com.forgerock.securebanking.platform.client.exceptions.ErrorType.INVALID_REQUEST;

@Controller
@Slf4j
@ComponentScan(basePackages = "com.forgerock.securebanking.platform.client.services")
public class ConsentDetailsApiController implements ConsentDetailsApi {

    private final ConsentServiceClient consentServiceClient;
    private final ApiClientServiceClient apiClientService;
    private final UserServiceClient userServiceClient;
    private final AccountService accountService;
    private final DomesticPaymentService domesticPaymentService;
    private final ConfigurationPropertiesClient configurationPropertiesClient;

    @Value("${rcs.consent.request.jwt.must-be-validated:false}")
    private Boolean jwtMustBeValidated;

    public ConsentDetailsApiController(ConsentServiceClient consentServiceClient,
                                       ApiClientServiceClient apiClientService,
                                       UserServiceClient userServiceClient,
                                       AccountService accountService, DomesticPaymentService domesticPaymentService,
                                       ConfigurationPropertiesClient configurationPropertiesClient) {
        this.consentServiceClient = consentServiceClient;
        this.apiClientService = apiClientService;
        this.userServiceClient = userServiceClient;
        this.accountService = accountService;
        this.domesticPaymentService = domesticPaymentService;
        this.configurationPropertiesClient = configurationPropertiesClient;
    }

    @Override
    public ResponseEntity<ConsentDetails> getConsentDetails(String consentRequestJws) throws InvalidConsentException {
        try {
            // TODO: the jwt should be validate here or in IG (JWTValidatorFilter)?
            if (jwtMustBeValidated) {
                JwtUtil.validateJWT(consentRequestJws, configurationPropertiesClient.getJwkUri());
            }

            SignedJWT signedJWT = JwtUtil.getSignedJWT(consentRequestJws);
            Claims claims = JwtUtil.getClaims(signedJWT);

            if (!claims.getIdTokenClaims().containsKey(Constants.Claims.INTENT_ID)) {
                log.error("(ConsentDetailsApiController#getConsentDetails) Missing Intent ID");
                throw new InvalidConsentException(consentRequestJws, INVALID_REQUEST,
                        OBRIErrorType.RCS_CONSENT_REQUEST_INVALID_CONSENT,
                        "Missing intent Id", null, null);
            }

            String intentId = JwtUtil.getIdTokenClaim(signedJWT, Constants.Claims.INTENT_ID);
            log.debug("Intent Id from the requested claims '{}'", intentId);
            switch (IntentType.identify(intentId)) {
                case ACCOUNT_ACCESS_CONSENT:
                    log.debug("Intent type: '{}' with ID '{}'", IntentType.ACCOUNT_ACCESS_CONSENT.name(), intentId);
                    AccountConsentRequest accountConsentRequest = buildAccountConsentRequest(signedJWT);
                    log.debug("Retrieve consent details:\n- Type '{}'\n-Id '{}'\n",
                            IntentType.identify(accountConsentRequest.getIntentId()).name(), accountConsentRequest.getIntentId());
                    Consent accountConsent = consentServiceClient.getConsent(accountConsentRequest);

                    log.debug("Retrieve to api client details for client Id '{}'", accountConsentRequest.getClientId());
                    ApiClient accountApiClient = apiClientService.getApiClient(accountConsentRequest.getClientId());

                    // build the consent details object for the response
                    ConsentDetails accountConsentDetails = ConsentDetailsBuilderFactory.build(accountConsent, accountConsentRequest, accountApiClient);

                    return ResponseEntity.ok(accountConsentDetails);

                case PAYMENT_DOMESTIC_CONSENT:
                    log.debug("Intent type: '{}' with ID '{}'", IntentType.PAYMENT_DOMESTIC_CONSENT.name(), intentId);
                    DomesticPaymentConsentRequest domesticPaymentConsentRequest = buildDomesticPaymentConsentRequest(signedJWT);
                    log.debug("Retrieve consent details:\n- Type '{}'\n-Id '{}'\n",
                            IntentType.identify(domesticPaymentConsentRequest.getIntentId()).name(), domesticPaymentConsentRequest.getIntentId());
                    Consent domesticPaymentConsent = consentServiceClient.getConsent(domesticPaymentConsentRequest);

                    log.debug("domesticPaymentConsent ", domesticPaymentConsent);

                    log.debug("Retrieve to api client details for client Id '{}'", domesticPaymentConsentRequest.getClientId());
                    ApiClient domesticPaymentApiClient = apiClientService.getApiClient(domesticPaymentConsentRequest.getClientId());

                    // build the consent details object for the response
                    ConsentDetails domesticPaymentConsentDetails = ConsentDetailsBuilderFactory.build(domesticPaymentConsent, domesticPaymentConsentRequest, domesticPaymentApiClient);

                    return ResponseEntity.ok(domesticPaymentConsentDetails);

                default:
                    String message = String.format("Invalid type for intent ID: '%s'", intentId);
                    log.error(message);
                    throw new ExceptionClient((ConsentDecision) null);
            }

        } catch (ExceptionClient e) {
            String errorMessage = String.format("%s", e.getMessage());
            log.error(errorMessage);
            throw new InvalidConsentException(consentRequestJws, e.getErrorClient().getErrorType(),
                    OBRIErrorType.REQUEST_BINDING_FAILED, errorMessage,
                    e.getErrorClient().getClientId(),
                    e.getErrorClient().getIntentId());
        }
    }

    private AccountConsentRequest buildAccountConsentRequest(SignedJWT signedJWT) throws ExceptionClient {
        String intentId = JwtUtil.getIdTokenClaim(signedJWT, Constants.Claims.INTENT_ID);
        log.debug("Intent Id from the requested claims '{}'", intentId);
        String clientId = JwtUtil.getClaimValue(signedJWT, Constants.Claims.CLIENT_ID);
        log.debug("Client Id from the JWT claims '{}'", clientId);
        String userId = JwtUtil.getClaimValue(signedJWT, Constants.Claims.USER_NAME);
        log.debug("User Id from the JWT claims '{}'", userId);
        List<FRAccountWithBalance> accounts = accountService.getAccountsWithBalance(userId);
        log.debug("Retrieve the user details for user Id '{}'", userId);
        User user = userServiceClient.getUser(userId);

        return AccountConsentRequest.builder()
                .intentId(intentId)
                .consentRequestJwt(signedJWT)
                .accounts(accounts)
                .user(user)
                .clientId(clientId)
                .build();
    }

    private DomesticPaymentConsentRequest buildDomesticPaymentConsentRequest(SignedJWT signedJWT) throws ExceptionClient {
        String intentId = JwtUtil.getIdTokenClaim(signedJWT, Constants.Claims.INTENT_ID);
        log.debug("Intent Id from the requested claims '{}'", intentId);
        String clientId = JwtUtil.getClaimValue(signedJWT, Constants.Claims.CLIENT_ID);
        log.debug("Client Id from the JWT claims '{}'", clientId);
        String userId = JwtUtil.getClaimValue(signedJWT, Constants.Claims.USER_NAME);
        log.debug("User Id from the JWT claims '{}'", userId);
        List<FRAccountWithBalance> accounts = accountService.getAccountsWithBalance(userId);
        log.debug("Retrieve the user details for user Id '{}'", userId);
        User user = userServiceClient.getUser(userId);

        return DomesticPaymentConsentRequest.builder()
                .intentId(intentId)
                .consentRequestJwt(signedJWT)
                .accounts(accounts)
                .user(user)
                .clientId(clientId)
                .build();
    }
}