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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.ConsentDecisionRequest;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.ConsentDecisionBuilderFactory;
import com.forgerock.securebanking.openbanking.uk.rcs.exception.InvalidConsentException;
import com.forgerock.securebanking.platform.client.Constants;
import com.forgerock.securebanking.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.Consent;
import com.forgerock.securebanking.platform.client.models.ConsentDecision;
import com.forgerock.securebanking.platform.client.services.ConsentServiceClient;
import com.forgerock.securebanking.platform.client.services.JwkServiceClient;
import com.forgerock.securebanking.platform.client.utils.jwt.JwtUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.text.ParseException;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_DECISION_EMPTY;
import static com.forgerock.securebanking.openbanking.uk.rcs.util.ConsentDecisionDeserializer.deserializeConsentDecision;
import static com.forgerock.securebanking.platform.client.exceptions.ErrorType.JWT_INVALID;

@Controller
@Slf4j
@ComponentScan(basePackages = "com.forgerock.securebanking.platform.client.services")
public class ConsentDecisionApiController implements ConsentDecisionApi {

    private final ObjectMapper objectMapper;
    private final ConsentServiceClient consentServiceClient;
    private final JwkServiceClient jwkServiceClient;

    public ConsentDecisionApiController(ObjectMapper objectMapper, ConsentServiceClient consentServiceClient, JwkServiceClient jwkServiceClient) {
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

        ConsentDecisionRequest consentDecisionRequest = deserializeConsentDecision(
                consentDecisionSerialised,
                objectMapper,
                ConsentDecisionRequest.class);

        try {

            boolean decision = Constants.ConsentDecision.AUTHORISED.equals(consentDecisionRequest.getDecision());
            log.debug("submitConsentDecision(consentDecisionSerialised) The decision is '{}'", decision);

            ConsentDecision consentDecision =
                    ConsentDecisionBuilderFactory.build(consentDecisionRequest);

            Consent consentUpdated = consentServiceClient.updateConsent(consentDecision);
            log.debug("submitConsentDecision(consentDecisionSerialised) Consent updated '{}", consentUpdated);

            JWTClaimsSet jwtClaimsSetGenerated = generateConsentResponse(decision, consentDecision);
            log.debug("submitConsentDecision(consentDecisionSerialised) jwt claims generated '{}'", jwtClaimsSetGenerated.toJSONObject());
            String consentSignedJwt = jwkServiceClient.signClaims(jwtClaimsSetGenerated, consentDecision.getIntentId());
            log.debug("submitConsentDecision(consentDecisionSerialised) consentSignedJwt '{}'", consentSignedJwt);
            String consentApprovalRedirectUri = JwtUtil.getClaimValue(consentSignedJwt, "consentApprovalRedirectUri");
            log.debug("submitConsentDecision(consentDecisionSerialised) CONSENT_APPROVAL_REDIRECT_URI: {} ", consentApprovalRedirectUri);
            return ResponseEntity.ok(RedirectionAction.builder()
                    .redirectUri(consentApprovalRedirectUri)
                    .consentJwt(consentSignedJwt)
                    .build());

        } catch (ExceptionClient e) {
            String errorMessage = String.format("%s", e.getMessage());
            log.error(errorMessage);
            throw new InvalidConsentException(consentDecisionRequest.getConsentJwt(), e.getErrorClient().getErrorType(),
                    OBRIErrorType.REQUEST_BINDING_FAILED, errorMessage,
                    e.getErrorClient().getClientId(),
                    e.getErrorClient().getIntentId());
        }
    }

    public JWTClaimsSet generateConsentResponse(
            boolean decision, ConsentDecision consentDecision
    ) throws ExceptionClient {
        try {
            JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(consentDecision.getJwtClaimsSet().toJSONObject());
            return new JWTClaimsSet.Builder(jwtClaimsSet)
                    .claim("decision", decision)
                    .claim("scopes", consentDecision.getScopes().toArray())
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
