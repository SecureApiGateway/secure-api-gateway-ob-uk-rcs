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
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.text.ParseException;
import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.common.api.meta.OBConstants.IdTokenClaim.INTENT_ID;
import static com.forgerock.securebanking.openbanking.uk.common.api.meta.OBConstants.OIDCClaim.CLIENT_ID;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_REQUEST_FORMAT;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_REQUEST_INVALID;

@Controller
@Slf4j
public class ConsentDetailsApiController implements ConsentDetailsApi {

    private final ConsentDetailsServiceDelegate consentDetailsServiceDelegate;
    private final UserProfileService userProfileService;
    private final AccountService accountService;

    public ConsentDetailsApiController(ConsentDetailsServiceDelegate consentDetailsServiceDelegate,
                                       UserProfileService userProfileService,
                                       AccountService accountService) {
        this.consentDetailsServiceDelegate = consentDetailsServiceDelegate;
        this.userProfileService = userProfileService;
        this.accountService = accountService;
    }

    @Override
    public ResponseEntity<ConsentDetails> getConsentDetails(String consentRequestJwt, String ssoToken)
            throws OBErrorException {

        try {
            log.debug("Parsing consent request JWS...");
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(consentRequestJwt);

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

            String username = userProfileService.getUsername(ssoToken);
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
}
