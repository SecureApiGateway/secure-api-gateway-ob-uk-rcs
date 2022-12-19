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
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.securebanking.openbanking.uk.rcs.exception.InvalidConsentException;
import com.forgerock.securebanking.platform.client.Constants;
import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.ConsentClientDecisionRequest;
import com.forgerock.securebanking.platform.client.models.ConsentClientDecisionRequestData;
import com.forgerock.securebanking.platform.client.services.ConsentServiceClient;
import com.forgerock.securebanking.platform.client.services.JwkServiceClient;
import com.forgerock.securebanking.platform.client.utils.jwt.JwtUtil;
import com.google.gson.JsonObject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.text.ParseException;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_DECISION_EMPTY;
import static com.forgerock.securebanking.openbanking.uk.rcs.util.ConsentDecisionDeserializer.deserializeConsentDecision;
import static com.forgerock.securebanking.platform.client.Constants.Claims.*;
import static com.forgerock.securebanking.platform.client.exceptions.ErrorType.JWT_INVALID;

@Controller
@Slf4j
@ComponentScan(basePackages = "com.forgerock.securebanking.platform.client.services")
public class ConsentDecisionApiController implements ConsentDecisionApi {

    private final ObjectMapper objectMapper;
    private final ConsentServiceClient consentServiceClient;
    private final JwkServiceClient jwkServiceClient;

    public ConsentDecisionApiController(
            ObjectMapper objectMapper,
            ConsentServiceClient consentServiceClient,
            JwkServiceClient jwkServiceClient) {
        this.objectMapper = objectMapper;
        this.consentServiceClient = consentServiceClient;
        this.jwkServiceClient = jwkServiceClient;
    }

    @Override
    public ResponseEntity<RedirectionAction> submitConsentDecision(String consentDecisionSerialised) throws OBErrorException {
        log.debug("submitConsentDecision(consentDecisionSerialised) '{}'", consentDecisionSerialised);
        if (consentDecisionSerialised == null) {
            log.debug("Consent decision is empty");
            throw new OBErrorException(RCS_CONSENT_DECISION_EMPTY);
        }

        ConsentDecisionDeserialized consentDecisionDeserialized = deserializeConsentDecision(
                consentDecisionSerialised,
                objectMapper,
                ConsentDecisionDeserialized.class
        );


        try {
            boolean decision = Constants.ConsentDecisionStatus.AUTHORISED.equals(consentDecisionDeserialized.getDecision());
            log.debug("The decision is '{}'", decision);
            SignedJWT signedJWT = JwtUtil.getSignedJWT(consentDecisionDeserialized.getConsentJwt());
            String intentId = JwtUtil.getIdTokenClaim(signedJWT, INTENT_ID);
            log.debug("Intent Id from the requested claims '{}'", intentId);
            String clientId = JwtUtil.getClaimValue(signedJWT, CLIENT_ID);
            log.debug("Client Id from the JWT claims '{}'", clientId);
            String resourceOwner = JwtUtil.getClaimValue(signedJWT, USER_NAME);
            log.debug("Resource owner from the JWT claims '{}'", resourceOwner);

            IntentType intentType = IntentType.identify(intentId);
            if (intentType != null) {

                ConsentClientDecisionRequest consentClientDecisionRequest = ConsentClientDecisionRequest.builder()
                        .accountIds(consentDecisionDeserialized.getAccountIds())
                        .accountId(
                                consentDecisionDeserialized.getDebtorAccount() != null ?
                                        consentDecisionDeserialized.getDebtorAccount().getAccountId() :
                                        null
                        ) // Backward compatibility //TODO needs to be deleted to use only the debtor account
                        .clientId(clientId)
                        .consentJwt(consentDecisionDeserialized.getConsentJwt())
                        .data(ConsentClientDecisionRequestData.builder()
                                .debtorAccount(
                                        consentDecisionDeserialized.getDebtorAccount() != null ?
                                                consentDecisionDeserialized.getDebtorAccount().getFirstAccount() :
                                                null
                                )
                                .status(consentDecisionDeserialized.getDecision())
                                .build())
                        .intentId(intentId)
                        .jwtClaimsSet(signedJWT.getJWTClaimsSet())
                        .resourceOwnerUsername(resourceOwner)
                        .scopes(
                                JwtUtil.getClaimValueMap(signedJWT, "scopes").values().stream().map(o -> (String) o).collect(Collectors.toList())
                        )
                        .build();

                JsonObject consentUpdated = consentServiceClient.updateConsent(consentClientDecisionRequest);
                log.debug("Consent updated '{}", consentUpdated);

                JWTClaimsSet jwtClaimsSetGenerated = generateJWTResponse(decision, consentClientDecisionRequest);
                log.debug("JWT claims generated '{}'", jwtClaimsSetGenerated.toJSONObject());

                String consentSignedJwt = jwkServiceClient.signClaims(jwtClaimsSetGenerated, consentClientDecisionRequest.getIntentId());
                log.debug("consentSignedJwt '{}'", consentSignedJwt);

                String consentApprovalRedirectUri = JwtUtil.getClaimValue(consentSignedJwt, "consentApprovalRedirectUri");
                log.debug("consentApprovalRedirectUri: {} ", consentApprovalRedirectUri);
                return ResponseEntity.ok(RedirectionAction.builder()
                        .redirectUri(consentApprovalRedirectUri)
                        .consentJwt(consentSignedJwt)
                        .build());
            } else {
                String message = String.format("Invalid type for intent ID: '%s'", intentId);
                log.error(message);
                throw new InvalidConsentException(
                        consentDecisionDeserialized.getConsentJwt(),
                        ErrorType.UNKNOWN_INTENT_TYPE,
                        OBRIErrorType.REQUEST_BINDING_FAILED,
                        message,
                        clientId,
                        intentId
                );
            }
        } catch (ExceptionClient | ParseException e) {
            String errorMessage = String.format("%s", e.getMessage());
            log.error(errorMessage);
            if (e instanceof ExceptionClient) {
                throw new InvalidConsentException(
                        consentDecisionDeserialized.getConsentJwt(),
                        ((ExceptionClient) e).getErrorClient().getErrorType(),
                        OBRIErrorType.REQUEST_BINDING_FAILED,
                        errorMessage,
                        ((ExceptionClient) e).getErrorClient().getClientId(),
                        ((ExceptionClient) e).getErrorClient().getIntentId()
                );
            } else {
                log.error("Could not parse the signedJWT to retrieve the claim set.", e);
                throw new InvalidConsentException(
                        consentDecisionDeserialized.getConsentJwt(),
                        JWT_INVALID,
                        OBRIErrorType.REQUEST_BINDING_FAILED,
                        errorMessage,
                        null,
                        null
                );
            }
        }
    }

    private JWTClaimsSet generateJWTResponse(
            boolean decision, ConsentClientDecisionRequest consentClientDecisionRequest
    ) throws ExceptionClient {
        try {
            JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(consentClientDecisionRequest.getJwtClaimsSet().toJSONObject());
            return new JWTClaimsSet.Builder(jwtClaimsSet)
                    .claim("decision", decision)
                    .claim("scopes", consentClientDecisionRequest.getScopes().toArray())
                    .expirationTime(DateTime.now().plusMinutes(5).toDate())
                    .issuer(jwtClaimsSet.getIssuer())
                    .audience(jwtClaimsSet.getIssuer())
                    .build();
        } catch (ParseException exception) {
            log.error("generateConsentResponse(decision, consentDecision) Could not parse the consentJwt from consent decision object.", exception);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(JWT_INVALID)
                            .build(),
                    String.format(JWT_INVALID.getDescription(), exception.getMessage()),
                    exception
            );
        }
    }
}
