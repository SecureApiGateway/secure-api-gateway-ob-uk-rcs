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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details;

import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType.INVALID_REQUEST;

import java.util.List;
import java.util.Objects;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.ConsentDetailsApi;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.PaymentsConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.factory.details.ConsentDetailsFactory;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.factory.details.ConsentDetailsFactoryProvider;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ConsentServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.UserServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.utils.jwt.JwtUtil;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.exception.InvalidConsentException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.forgerock.sapi.gateway.uk.common.shared.claim.Claims;
import com.google.gson.JsonObject;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@ComponentScan(basePackages = {"com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services", "com.forgerock.sapi.gateway.ob.uk.rcs.api"})
public class ConsentDetailsApiController implements ConsentDetailsApi {

    private final ObjectMapper objectMapper;
    private final ConsentServiceClient consentServiceClient;
    private final ApiClientServiceClient apiClientService;
    private final UserServiceClient userServiceClient;
    private final AccountService accountService;
    private final ApiProviderConfiguration apiProviderConfiguration;
    private final ConsentDetailsFactoryProvider consentDetailsFactoryProvider;
    private final ConsentStoreDetailsServiceRegistry consentStoreDetailsServiceRegistry;

    public ConsentDetailsApiController(ConsentServiceClient consentServiceClient,
                                       ApiClientServiceClient apiClientService,
                                       UserServiceClient userServiceClient,
                                       AccountService accountService,
                                       ObjectMapper objectMapper,
                                       ApiProviderConfiguration apiProviderConfiguration,
                                       ConsentDetailsFactoryProvider consentDetailsFactoryProvider,
                                       ConsentStoreDetailsServiceRegistry consentStoreDetailsServiceRegistry) {
        this.consentServiceClient = consentServiceClient;
        this.apiClientService = apiClientService;
        this.userServiceClient = userServiceClient;
        this.accountService = accountService;
        this.objectMapper = objectMapper;
        this.apiProviderConfiguration = apiProviderConfiguration;
        this.consentDetailsFactoryProvider = consentDetailsFactoryProvider;
        this.consentStoreDetailsServiceRegistry = consentStoreDetailsServiceRegistry;
    }

    @Override
    public ResponseEntity<ConsentDetails> getConsentDetails(String consentRequestJws) throws InvalidConsentException {
        String intentId = null;
        String apiClientId = null;
        try {
            SignedJWT signedJWT = JwtUtil.getSignedJWT(consentRequestJws);
            Claims claims = JwtUtil.getClaims(signedJWT);

            if (!claims.getIdTokenClaims().containsKey(Constants.Claims.INTENT_ID)) {
                log.error("(ConsentDetailsApiController#getConsentDetails) Missing Intent ID");
                throw new InvalidConsentException(consentRequestJws, INVALID_REQUEST,
                        OBRIErrorType.RCS_CONSENT_REQUEST_INVALID_CONSENT,
                        "Missing intent Id", null, null);
            }

            intentId = JwtUtil.getIdTokenClaim(signedJWT, Constants.Claims.INTENT_ID);
            log.debug("Intent Id from the requested claims '{}'", intentId);

            ConsentClientDetailsRequest consentClientRequest = buildConsentClientRequest(signedJWT);
            log.debug("Intent type: '{}' with ID '{}'", IntentType.identify(intentId), intentId);

            apiClientId = consentClientRequest.getClientId();

            IntentType intentType = IntentType.identify(intentId);
            if (Objects.nonNull(intentType)) {
                final ConsentDetails details;
                if (consentStoreDetailsServiceRegistry.isIntentTypeSupported(intentType)) {
                    details = consentStoreDetailsServiceRegistry.getDetailsFromConsentStore(intentType, consentClientRequest);
                } else {

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
                    details = consentDetailsFactory.decode(consent);
                    details.setConsentId(intentId);
                    // the api provider name (aspsp, aisp), usually a bank
                    details.setServiceProviderName(apiProviderConfiguration.getName());
                    // the client Name (TPP name)
                    details.setClientName(apiClient.getName());
                    details.setUsername(consentClientRequest.getUser().getUserName());
                    details.setUserId(consentClientRequest.getUser().getId());

                    // Initiation payment case
                    // DebtorAccount is optional, but the PISP could provide the account identifier details for the PSU
                    // The accounts displayed in the RCS ui needs to be The debtor account if is provided in the consent otherwise the user accounts
                    if ((details instanceof PaymentsConsentDetails) && Objects.nonNull(((PaymentsConsentDetails) details).getDebtorAccount())) {
                        setDebtorAccountWithBalance((PaymentsConsentDetails) details, consentRequestJws, intentId);
                    } else {
                        details.setAccounts(accountService.getAccountsWithBalance(details.getUserId()));
                    }

                    details.setClientId(consentClientRequest.getClientId());
                    details.setLogo(apiClient.getLogoUri());
                }
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
        } catch (ConsentStoreException cse) {
            log.error("Failed to get Consent Details due to ConsentStoreException", cse);
            throw new InvalidConsentException(consentRequestJws, ErrorType.INTERNAL_SERVER_ERROR, OBRIErrorType.REQUEST_BINDING_FAILED,
                                              "Internal Server Error", apiClientId, intentId);
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

    public void setDebtorAccountWithBalance(PaymentsConsentDetails details, String consentRequestJws, String intentId) {
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
}
