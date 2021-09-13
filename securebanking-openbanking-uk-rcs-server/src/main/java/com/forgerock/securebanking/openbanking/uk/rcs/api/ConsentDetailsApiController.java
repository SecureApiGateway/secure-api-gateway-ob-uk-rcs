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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.openbanking.uk.common.claim.Claims;
import com.forgerock.securebanking.openbanking.uk.common.claim.JwsClaimsUtils;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.am.UserProfileService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.rs.AccountService;
import com.forgerock.securebanking.openbanking.uk.rcs.service.detail.ConsentDetailsRequest;
import com.forgerock.securebanking.openbanking.uk.rcs.service.detail.ConsentDetailsServiceDelegate;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.common.api.meta.OBConstants.IdTokenClaim.INTENT_ID;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.common.RcsConstants.Claims.CLIENT_ID;
import static com.forgerock.securebanking.openbanking.uk.rcs.common.RcsConstants.Claims.USER_NAME;

@Controller
@Slf4j
public class ConsentDetailsApiController implements ConsentDetailsApi {

    private final ConsentDetailsServiceDelegate consentDetailsServiceDelegate;
    private final UserProfileService userProfileService;
    private final AccountService accountService;

    @Value("${rcs.consent.request.jwt.must-be-validated:false}")
    private Boolean jwtMustBeValidated;

    public ConsentDetailsApiController(ConsentDetailsServiceDelegate consentDetailsServiceDelegate,
                                       UserProfileService userProfileService,
                                       AccountService accountService) {
        this.consentDetailsServiceDelegate = consentDetailsServiceDelegate;
        this.userProfileService = userProfileService;
        this.accountService = accountService;
    }

    @Override
    public ResponseEntity<ConsentDetails> getConsentDetails(String consentRequestJwt)
            throws OBErrorException {
        // TODO: the jwt should be validate here or in IG (JWTValidatorFilter)?
        try {

            log.debug("Parsing consent request JWS...");
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(consentRequestJwt);
            if (jwtMustBeValidated) {
                ValidateJWT(signedJWT);
            }
            log.debug("Reading Intent ID from the claims...");
            // Read the claims
            Claims claims = JwsClaimsUtils.getClaims(signedJWT);

            if (!claims.getIdTokenClaims().containsKey(INTENT_ID)) {
                throw new OBErrorException(RCS_CONSENT_REQUEST_INVALID, "No intent ID");
            }
            String intentId = claims.getIdTokenClaims().get(INTENT_ID).getValue();
            log.debug("Intent Id from the requested claims '{}'", intentId);
            String clientId = signedJWT.getJWTClaimsSet().getStringClaim(CLIENT_ID);
            log.debug("Client Id from the JWT claims '{}'", clientId);
            String username = signedJWT.getJWTClaimsSet().getStringClaim(USER_NAME);
            log.debug("Username from the JWT claims '{}'", username);
            // TODO: we not need call the am to get the user profile
            // String username = userProfileService.getUsername(username);
            List<FRAccountWithBalance> accounts = accountService.getAccountsWithBalance(username);

            ConsentDetailsRequest detailsRequest = ConsentDetailsRequest.builder()
                    .intentId(intentId)
                    .consentRequestJwt(signedJWT)
                    .accounts(accounts)
                    .username(username)
                    .clientId(clientId)
                    .build();
            ConsentDetails consentDetails = consentDetailsServiceDelegate.getConsentDetails(detailsRequest);
            return ResponseEntity.ok(consentDetails);

        } catch (ParseException e) {
            log.error("Could not parse the JWT", e);
            throw new OBErrorException(RCS_CONSENT_REQUEST_FORMAT);
        }
    }

    // TODO: must be moved a util library (method not finished yet)
    private void ValidateJWT(String jwt) throws OBErrorException {
        try {
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(jwt);
            ValidateJWT(signedJWT);
        } catch (ParseException e) {
            log.error("Could not parse the JWT", e);
            throw new OBErrorException(RCS_CONSENT_REQUEST_FORMAT);
        }
    }

    private void ValidateJWT(SignedJWT signedJWT) throws OBErrorException {
        // TODO: create a config parameter in config server, for the moment is hardcoded for tests purposes
        String jwk_uri = "https://iam.dev.forgerock.financial/am/oauth2/connect/jwk_uri";
        try {
            if (signedJWT.getHeader().getAlgorithm() != null && jwk_uri != null) {
                JWSAlgorithm expectedJWSAlg = JWSAlgorithm.parse(signedJWT.getHeader().getAlgorithm().getName());
                JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwk_uri));
                JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);

                // Create a JWT processor
                ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
                jwtProcessor.setJWSKeySelector(keySelector);
                JWTClaimsSetVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>();
                jwtProcessor.setJWTClaimsSetVerifier(claimsVerifier);

                // Process the JWT
                SecurityContext ctx = null; // optional context parameter, not required here
                jwtProcessor.process(signedJWT, ctx);
            }
        } catch (BadJOSEException | JOSEException | MalformedURLException e) {
            String messageError = String.format("(%s) Error verifying the consent request JWT. Reason: %s", this.getClass().getSimpleName(), e.getMessage());
            log.error(messageError);
            throw new OBErrorException(REQUEST_PARAMETER_JWT_INVALID, e.getMessage());
        }
    }
}
