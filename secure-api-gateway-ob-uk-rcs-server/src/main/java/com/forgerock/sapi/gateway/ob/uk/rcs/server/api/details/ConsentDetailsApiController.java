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

import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType.INVALID_REQUEST;

import java.util.Objects;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.ConsentDetailsApi;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.UserServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.utils.jwt.JwtUtil;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.exception.InvalidConsentException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.forgerock.sapi.gateway.uk.common.shared.claim.Claims;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@ComponentScan(basePackages = {"com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services", "com.forgerock.sapi.gateway.ob.uk.rcs.api"})
public class ConsentDetailsApiController implements ConsentDetailsApi {

    private final UserServiceClient userServiceClient;
    private final ConsentStoreDetailsServiceRegistry consentStoreDetailsServiceRegistry;

    public ConsentDetailsApiController(UserServiceClient userServiceClient,
                                       ConsentStoreDetailsServiceRegistry consentStoreDetailsServiceRegistry) {
        this.userServiceClient = userServiceClient;
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
            log.debug("XXX: intentType={}", intentType != null ? intentType.name() : "null");
            if (Objects.nonNull(intentType)) {
                final ConsentDetails details;
                if (consentStoreDetailsServiceRegistry.isIntentTypeSupported(intentType)) {
                    log.debug("XXX: isIntentTypeSupported({})=true", intentType.name());
                    details = consentStoreDetailsServiceRegistry.getDetailsFromConsentStore(intentType, consentClientRequest);
                } else {
                    log.debug("XXX: isIntentTypeSupported({})=false", intentType.name());
                    throw new IllegalStateException(intentType + " not supported");
                }
                return ResponseEntity.ok(details);
            } else {
                String message = String.format("Invalid type for intent ID: '%s'", intentId);
                log.error(message);
                throw new ExceptionClient(consentClientRequest, ErrorType.UNKNOWN_INTENT_TYPE, message);
            }
        } catch (ExceptionClient e) {
            log.error("XXX: ExceptionClient raised: '{}'", e.getMessage(), e);
            String errorMessage = String.format("%s", e.getMessage());
            log.error(errorMessage);
            throw new InvalidConsentException(consentRequestJws, e.getErrorClient().getErrorType(),
                    OBRIErrorType.REQUEST_BINDING_FAILED, errorMessage,
                    e.getErrorClient().getClientId(),
                    e.getErrorClient().getIntentId());
        } catch (ConsentStoreException cse) {
            throw buildInvalidConsentException(consentRequestJws, intentId, apiClientId, cse);
        }
    }

    private static InvalidConsentException buildInvalidConsentException(String consentRequestJws, String intentId, String apiClientId, ConsentStoreException cse) {
        log.error("Failed to get Consent Details due to ConsentStoreException", cse);
        final ErrorType errorType;
        final String errorMessage;
        switch (cse.getErrorType()) {
            case NOT_FOUND -> {
                errorType = ErrorType.NOT_FOUND;
                errorMessage = "Consent Not Found";
            }
            case INVALID_PERMISSIONS -> {
                errorType = ErrorType.ACCESS_DENIED;
                errorMessage = "Access Denied";
            }
            case CONSENT_REAUTHENTICATION_NOT_SUPPORTED -> {
                errorType = ErrorType.ACCESS_DENIED;
                errorMessage = "Consent Re-Authentication not supported for this type of consent";
            }
            case INVALID_DEBTOR_ACCOUNT -> {
                errorType = ErrorType.ACCESS_DENIED;
                errorMessage = "User is not permissioned to make a payment from the debtorAccount specified";
            }
            default -> {
                errorType = ErrorType.INTERNAL_SERVER_ERROR;
                errorMessage = "Server Error";
            }
        }
        return new InvalidConsentException(consentRequestJws, errorType, OBRIErrorType.REQUEST_BINDING_FAILED,
                                           errorMessage, apiClientId, intentId);
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
