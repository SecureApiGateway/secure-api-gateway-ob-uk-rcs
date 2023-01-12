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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAccountIdentifier;
import com.forgerock.securebanking.openbanking.uk.common.claim.Claims;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticVrpPaymentConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.rs.AccountService;
import com.forgerock.securebanking.openbanking.uk.rcs.configuration.ApiProviderConfiguration;
import com.forgerock.securebanking.openbanking.uk.rcs.exception.InvalidConsentException;
import com.forgerock.securebanking.openbanking.uk.rcs.factory.details.ConsentDetailsFactory;
import com.forgerock.securebanking.openbanking.uk.rcs.factory.details.ConsentDetailsFactoryProvider;
import com.forgerock.securebanking.platform.client.Constants;
import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.configuration.ConfigurationPropertiesClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.ApiClient;
import com.forgerock.securebanking.platform.client.models.ConsentClientDetailsRequest;
import com.forgerock.securebanking.platform.client.models.User;
import com.forgerock.securebanking.platform.client.services.ApiClientServiceClient;
import com.forgerock.securebanking.platform.client.services.ConsentServiceClient;
import com.forgerock.securebanking.platform.client.services.UserServiceClient;
import com.forgerock.securebanking.platform.client.utils.jwt.JwtUtil;
import com.google.gson.JsonObject;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Objects;

import static com.forgerock.securebanking.platform.client.exceptions.ErrorType.INVALID_REQUEST;

@Controller
@Slf4j
@ComponentScan(basePackages = "com.forgerock.securebanking.platform.client.services")
public class ConsentDetailsApiController implements ConsentDetailsApi {

    private final ObjectMapper objectMapper;
    private final ConsentServiceClient consentServiceClient;
    private final ApiClientServiceClient apiClientService;
    private final UserServiceClient userServiceClient;
    private final AccountService accountService;
    private final ConfigurationPropertiesClient configurationPropertiesClient;
    private final ApiProviderConfiguration apiProviderConfiguration;
    //    private final ConsentDetailsFactoryOld consentDetailsFactory;
    private final ConsentDetailsFactoryProvider consentDetailsFactoryProvider;

    @Value("${rcs.consent.request.jwt.must-be-validated:false}")
    private Boolean jwtMustBeValidated;

    public ConsentDetailsApiController(ConsentServiceClient consentServiceClient,
                                       ApiClientServiceClient apiClientService,
                                       UserServiceClient userServiceClient,
                                       AccountService accountService,
                                       ConfigurationPropertiesClient configurationPropertiesClient,
                                       ObjectMapper objectMapper,
                                       ApiProviderConfiguration apiProviderConfiguration,
                                       ConsentDetailsFactoryProvider consentDetailsFactoryProvider
    ) {
        this.consentServiceClient = consentServiceClient;
        this.apiClientService = apiClientService;
        this.userServiceClient = userServiceClient;
        this.accountService = accountService;
        this.configurationPropertiesClient = configurationPropertiesClient;
        this.objectMapper = objectMapper;
        this.apiProviderConfiguration = apiProviderConfiguration;
        this.consentDetailsFactoryProvider = consentDetailsFactoryProvider;
    }

    @Override
    public ResponseEntity<ConsentDetails> getConsentDetails(String consentRequestJws) throws InvalidConsentException {
        try {
            // TODO: the jwt should be validated here or in IG (JWTValidatorFilter)?
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

            ConsentClientDetailsRequest consentClientRequest = buildConsentClientRequest(signedJWT);
            log.debug("Intent type: '{}' with ID '{}'", IntentType.identify(intentId), intentId);

            IntentType intentType = IntentType.identify(intentId);
            if (Objects.nonNull(intentType)) {
                log.debug("Intent type: '{}' with ID '{}'", intentType, intentId);
                log.debug("Retrieve consent details:\n- Type '{}'\n-Id '{}'\n",
                        intentType.name(), consentClientRequest.getIntentId());
                JsonObject consent = consentServiceClient.getConsent(consentClientRequest);

                log.debug("Retrieve to api client details for client Id '{}'", consentClientRequest.getClientId());
                ApiClient apiClient = apiClientService.getApiClient(consentClientRequest.getClientId());
                log.debug("ApiClient controller: " + apiClient);
                log.debug("consent json controller: " + consent);
                // consent details object thread safe instance by intent type
                ConsentDetailsFactory consentDetailsFactory = consentDetailsFactoryProvider.getFactory(intentType);
                ConsentDetails details = consentDetailsFactory.decode(consent);
                details.setConsentId(intentId);
                // the api provider name (aspsp, aisp), usually a bank
                details.setServiceProviderName(apiProviderConfiguration.getName());
                // the client Name (TPP name)
                details.setClientName(apiClient.getName());
                details.setUsername(consentClientRequest.getUser().getUserName());
                details.setUserId(consentClientRequest.getUser().getId());

                // DebtorAccount is optional, but the PISP could provide the account identifier details for the PSU
                // The accounts displayed in the RCS ui needs to be The debtor account if is provided in the consent otherwise the user accounts
                // TODO debtorAccount logic for VRP payments until fix  https://github.com/secureapigateway/secureapigateway/issues/731
                if (Objects.nonNull(details.getDebtorAccount()) & details.getIntentType().equals(IntentType.DOMESTIC_VRP_PAYMENT_CONSENT)) {
                    setDebtorAccountWithBalance(details, consentRequestJws, intentId);
                } else {
                    details.setAccounts(accountService.getAccountsWithBalance(details.getUserId()));
                }

                details.setClientId(consentClientRequest.getClientId());
                details.setLogo(apiClient.getLogoUri());
                return ResponseEntity.ok(details);

            } else {
                String message = String.format("Invalid type for intent ID: '%s'", intentId);
                log.error(message);
                throw new ExceptionClient(consentClientRequest, ErrorType.UNKNOWN_INTENT_TYPE, message);
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

    /*
        DebtorAccount is optional, but the PISP could provide the account identification details for the PSU.
        If the account identifier match with an existing one for that psu then we update the debtor account with the proper accountId
        and set the debtor account with balance as accounts to be display in the consent UI
     */
    private void setDebtorAccountWithBalance(ConsentDetails details, String consentRequestJws, String intentId) {
        FRAccountIdentifier debtorAccount = details.getDebtorAccount();
        if (Objects.nonNull(debtorAccount)) {
            FRAccountWithBalance accountWithBalance = accountService.getAccountWithBalanceByIdentifiers(
                    details.getUserId(), debtorAccount.getName(), debtorAccount.getIdentification(), debtorAccount.getSchemeName()
            );
            if (Objects.nonNull(accountWithBalance)) {
                debtorAccount.setAccountId(accountWithBalance.getAccount().getAccountId());
                details.setAccounts(List.of(accountWithBalance));
            } else {
                String message = String.format("Invalid debtor account provide in the consent for the intent ID: '%s', the debtor account provided in the consent doesn't exist", intentId);
                log.error(message);
                throw new InvalidConsentException(consentRequestJws, ErrorType.ACCOUNT_SELECTION_REQUIRED,
                        OBRIErrorType.REQUEST_BINDING_FAILED, message,
                        details.getClientId(),
                        intentId);
            }
        }
    }

    private ConsentClientDetailsRequest buildConsentClientRequest(SignedJWT signedJWT) throws ExceptionClient {
        String intentId = JwtUtil.getIdTokenClaim(signedJWT, Constants.Claims.INTENT_ID);
        log.debug("Intent Id from the requested claims '{}'", intentId);
        String clientId = JwtUtil.getClaimValue(signedJWT, Constants.Claims.CLIENT_ID);
        log.debug("Client Id from the JWT claims '{}'", clientId);
        String userId = JwtUtil.getClaimValue(signedJWT, Constants.Claims.USER_NAME);
        log.debug("User Id from the JWT claims '{}'", userId);
        log.debug("Retrieve the user details for user Id '{}'", userId);
        User user = userServiceClient.getUser(userId);

        return ConsentClientDetailsRequest.builder()
                .intentId(intentId)
                .consentRequestJwt(signedJWT)
                .user(user)
                .clientId(clientId)
                .build();
    }
}
