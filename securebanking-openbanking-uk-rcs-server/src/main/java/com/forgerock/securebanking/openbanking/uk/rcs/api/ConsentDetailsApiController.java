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
import com.forgerock.securebanking.openbanking.uk.rcs.converters.general.ConsentDetailsBuilderFactory;
import com.forgerock.securebanking.openbanking.uk.rcs.exception.InvalidConsentException;
import com.forgerock.securebanking.platform.client.Constants;
import com.forgerock.securebanking.platform.client.configuration.ConfigurationPropertiesClient;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.base.ApiClient;
import com.forgerock.securebanking.platform.client.models.base.ConsentDecision;
import com.forgerock.securebanking.platform.client.models.base.ConsentRequest;
import com.forgerock.securebanking.platform.client.models.base.User;
import com.forgerock.securebanking.platform.client.services.ApiClientServiceClient;
import com.forgerock.securebanking.platform.client.services.ConsentServiceClient;
import com.forgerock.securebanking.platform.client.services.UserServiceClient;
import com.forgerock.securebanking.platform.client.utils.jwt.JwtUtil;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.forgerock.json.JsonValue;
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
    private final ConfigurationPropertiesClient configurationPropertiesClient;

    @Value("${rcs.consent.request.jwt.must-be-validated:false}")
    private Boolean jwtMustBeValidated;

    public ConsentDetailsApiController(ConsentServiceClient consentServiceClient,
                                       ApiClientServiceClient apiClientService,
                                       UserServiceClient userServiceClient,
                                       AccountService accountService,
                                       ConfigurationPropertiesClient configurationPropertiesClient) {
        this.consentServiceClient = consentServiceClient;
        this.apiClientService = apiClientService;
        this.userServiceClient = userServiceClient;
        this.accountService = accountService;
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

            if (IntentType.identify(intentId) != null) {
                log.debug("Intent type: '{}' with ID '{}'", IntentType.identify(intentId), intentId);
                ConsentRequest consentRequest = buildConsentRequest(signedJWT);
                log.debug("Retrieve consent details:\n- Type '{}'\n-Id '{}'\n",
                        IntentType.identify(consentRequest.getIntentId()).name(), consentRequest.getIntentId());
                JsonValue consent = consentServiceClient.getConsent(consentRequest);

                log.debug("Retrieve to api client details for client Id '{}'", consentRequest.getClientId());
                ApiClient apiClient = apiClientService.getApiClient(consentRequest.getClientId());

                // build the consent details object for the response
                ConsentDetails consentDetails = ConsentDetailsBuilderFactory.build(consent, consentRequest, apiClient);

                return ResponseEntity.ok(consentDetails);

            } else {
                String message = String.format("Invalid type for intent ID: '%s'", intentId);
                log.error(message);
                //TODO
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

    private ConsentRequest buildConsentRequest(SignedJWT signedJWT) throws ExceptionClient {
        String intentId = JwtUtil.getIdTokenClaim(signedJWT, Constants.Claims.INTENT_ID);
        log.debug("Intent Id from the requested claims '{}'", intentId);
        String clientId = JwtUtil.getClaimValue(signedJWT, Constants.Claims.CLIENT_ID);
        log.debug("Client Id from the JWT claims '{}'", clientId);
        String userId = JwtUtil.getClaimValue(signedJWT, Constants.Claims.USER_NAME);
        log.debug("User Id from the JWT claims '{}'", userId);
        List<FRAccountWithBalance> accounts = accountService.getAccountsWithBalance(userId);
        log.debug("Retrieve the user details for user Id '{}'", userId);
        User user = userServiceClient.getUser(userId);

        return ConsentRequest.builder()
                .intentId(intentId)
                .consentRequestJwt(signedJWT)
                .accounts(accounts)
                .user(user)
                .clientId(clientId)
                .build();
    }
}