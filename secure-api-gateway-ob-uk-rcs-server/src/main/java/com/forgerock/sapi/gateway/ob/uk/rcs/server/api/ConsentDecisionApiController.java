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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.ConsentDecisionApi;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.exception.InvalidConsentException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.IntentType;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.jwt.RcsJwtSigner;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDecisionRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDecisionRequestData;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ConsentServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.utils.jwt.JwtUtil;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.text.ParseException;
import java.util.stream.Collectors;

import static com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType.RCS_CONSENT_DECISION_EMPTY;
import static com.forgerock.sapi.gateway.ob.uk.rcs.server.util.ConsentDecisionDeserializer.deserializeConsentDecision;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType.INTERNAL_SERVER_ERROR;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType.JWT_INVALID;

@Controller
@Slf4j
@ComponentScan(basePackages = "com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services")
public class ConsentDecisionApiController implements ConsentDecisionApi {

    private final ObjectMapper objectMapper;
    private final ConsentServiceClient consentServiceClient;
    private final RcsJwtSigner jwtSigner;
    private final String rcsJwtIssuer;

    public ConsentDecisionApiController(ObjectMapper objectMapper, ConsentServiceClient consentServiceClient,
            RcsJwtSigner jwtSigner, @Value("${rcs.consent.response.jwt.issuer}") String rcsJwtIssuer) {
        this.objectMapper = objectMapper;
        this.consentServiceClient = consentServiceClient;
        this.jwtSigner = jwtSigner;
        this.rcsJwtIssuer = rcsJwtIssuer;
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

        log.debug("decision deserialised \n {}", consentDecisionDeserialized);
        try {
            boolean decision = Constants.ConsentDecisionStatus.AUTHORISED.equals(consentDecisionDeserialized.getDecision());
            log.debug("The decision is '{}'", decision);
            SignedJWT signedJWT = JwtUtil.getSignedJWT(consentDecisionDeserialized.getConsentJwt());
            String intentId = JwtUtil.getIdTokenClaim(signedJWT, Constants.Claims.INTENT_ID);
            log.debug("Intent Id from the requested claims '{}'", intentId);
            String clientId = JwtUtil.getClaimValue(signedJWT, Constants.Claims.CLIENT_ID);
            log.debug("Client Id from the JWT claims '{}'", clientId);
            String resourceOwner = JwtUtil.getClaimValue(signedJWT, Constants.Claims.USER_NAME);
            log.debug("Resource owner from the JWT claims '{}'", resourceOwner);

            IntentType intentType = IntentType.identify(intentId);
            if (intentType != null) {
                ConsentClientDecisionRequest consentClientDecisionRequest = ConsentClientDecisionRequest.builder()
                        .accountIds(consentDecisionDeserialized.getAccountIds())
                        .clientId(clientId)
                        .consentJwt(consentDecisionDeserialized.getConsentJwt())
                        .data(ConsentClientDecisionRequestData.builder()
                                .debtorAccount(
                                        consentDecisionDeserialized.getDebtorAccount() != null ?
                                                consentDecisionDeserialized.getDebtorAccount() :
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
                log.debug("consentClientDecisionRequest \n {}", consentClientDecisionRequest);
                JsonObject consentUpdated = consentServiceClient.updateConsent(consentClientDecisionRequest);
                log.debug("Consent updated '{}", consentUpdated);

                JWTClaimsSet jwtClaimsSetGenerated = generateJWTResponse(decision, consentClientDecisionRequest);
                log.debug("JWT claims generated '{}'", jwtClaimsSetGenerated.toJSONObject());
                String consentSignedJwt = jwtSigner.createSignedJwt(jwtClaimsSetGenerated);
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
        } catch (JOSEException e) {
            final String errorMessage = "Failed to sign consent decision response JWT";
            log.error(errorMessage, e);
            throw new InvalidConsentException(consentDecisionDeserialized.getConsentJwt(), INTERNAL_SERVER_ERROR,
                                              OBRIErrorType.RCS_CONSENT_RESPONSE_FAILURE, errorMessage, null, null);
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
                    .issuer(rcsJwtIssuer)
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
