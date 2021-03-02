/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
import com.forgerock.securebanking.openbanking.uk.common.claim.Claims;
import com.forgerock.securebanking.openbanking.uk.common.claim.JwsClaimsUtils;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.ConsentDecision;
import com.forgerock.securebanking.openbanking.uk.rcs.common.RCSConstants;
import com.forgerock.securebanking.openbanking.uk.rcs.service.decision.ConsentDecisionService;
import com.forgerock.securebanking.openbanking.uk.rcs.service.decision.ConsentDecisionServiceDelegate;
import com.forgerock.securebanking.openbanking.uk.rcs.validator.ConsentDecisionValidator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.text.ParseException;

import static com.forgerock.securebanking.openbanking.uk.common.api.meta.OBConstants.IdTokenClaim.INTENT_ID;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_DECISION_EMPTY;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_REQUEST_FORMAT;
import static com.forgerock.securebanking.openbanking.uk.rcs.common.RCSConstants.Claims.CLIENT_ID;
import static com.forgerock.securebanking.openbanking.uk.rcs.util.ConsentDecisionDeserializer.deserializeConsentDecision;

@Controller
@Slf4j
public class ConsentDecisionApiController implements ConsentDecisionApi {

    private final ConsentDecisionResponseHandler consentDecisionResponseHandler;
    private final ObjectMapper objectMapper;
    private final ConsentDecisionServiceDelegate consentServiceDelegate;
    private final ConsentDecisionValidator consentDecisionValidator;

    public ConsentDecisionApiController(ConsentDecisionResponseHandler consentDecisionResponseHandler,
                                        ObjectMapper objectMapper,
                                        ConsentDecisionServiceDelegate consentServiceDelegate,
                                        ConsentDecisionValidator consentDecisionValidator) {
        this.consentDecisionResponseHandler = consentDecisionResponseHandler;
        this.objectMapper = objectMapper;
        this.consentServiceDelegate = consentServiceDelegate;
        this.consentDecisionValidator = consentDecisionValidator;
    }

    @Override
    public ResponseEntity<RedirectionAction> submitConsentDecision(String consentDecisionSerialised,
                                                                   String ssoToken) throws OBErrorException {
        if (consentDecisionSerialised == null) {
            log.debug("Consent decision is empty");
            throw new OBErrorException(RCS_CONSENT_DECISION_EMPTY);
        }
        log.debug("Received a consent decision request");

        try {
            ConsentDecision consentDecision = deserializeConsentDecision(consentDecisionSerialised, objectMapper, ConsentDecision.class);
            String consentRequestJwt = consentDecision.getConsentJwt();
            SignedJWT consentContextJwt = (SignedJWT) JWTParser.parse(consentRequestJwt);
            boolean decision = RCSConstants.Decision.ALLOW.equals(consentDecision.getDecision());
            log.debug("The decision is '{}'", decision);

            Claims claims = JwsClaimsUtils.getClaims(consentContextJwt);
            String intentId = claims.getIdTokenClaims().get(INTENT_ID).getValue();
            JWTClaimsSet jwtClaimsSet = consentContextJwt.getJWTClaimsSet();
            String clientId = jwtClaimsSet.getStringClaim(CLIENT_ID);

            // Get the right decision service, cased on the intent type
            ConsentDecisionService consentDecisionService = consentServiceDelegate.getConsentDecisionService(intentId);
            consentDecisionValidator.validateConsent(intentId, clientId, ssoToken, consentDecisionService);
            consentDecisionService.processConsentDecision(intentId, consentDecisionSerialised, decision);

            return consentDecisionResponseHandler.handleResponse(ssoToken, consentContextJwt, decision, jwtClaimsSet, clientId);
        } catch (ParseException e) {
            log.error("Could not parse the JWT", e);
            throw new OBErrorException(RCS_CONSENT_REQUEST_FORMAT);
        }
    }
}
