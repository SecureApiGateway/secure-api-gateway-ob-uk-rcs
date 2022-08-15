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
package com.forgerock.securebanking.openbanking.uk.rcs.converters.general;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.ConsentDecisionRequest;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.ConsentDecision;
import com.forgerock.securebanking.platform.client.utils.jwt.JwtUtil;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.platform.client.Constants.Claims.*;
import static com.forgerock.securebanking.platform.client.exceptions.ErrorType.JWT_INVALID;
import static java.util.Objects.requireNonNull;

/**
 * Factory to build the rcs {@link ConsentDetails} object from the Platform Consent
 */
@Slf4j
public class ConsentDecisionBuilderFactory {

    public static final ConsentDecision build(ConsentDecisionRequest consentDecision) throws ExceptionClient {
        requireNonNull(consentDecision, "build(consentDecision) parameter 'consentDecision' cannot be null");
        return buildConsentDecision(consentDecision);
    }

    private static final ConsentDecision buildConsentDecision(ConsentDecisionRequest consentDecisionRequest) throws ExceptionClient {
        try {
            ConsentDecisionConverter consentDecisionConverter = ConsentDecisionConverter.getInstance();
            ConsentDecision decision = consentDecisionConverter.toConsentDecision(consentDecisionRequest);
            log.debug("ConsentDecision: '{}'", decision);
            log.debug("ConsentDecisionRequest: '{}'", consentDecisionRequest);
            try {
                String accountId = consentDecisionRequest.getDebtorAccount().getAccountId();
                log.debug("The account Id of the debtor account id: '{}'", accountId);
                decision.getData().getDebtorAccount().setAccountId(accountId);
                log.debug("The the debtor account with the id: '{}'", decision.getData().getDebtorAccount());
            } catch (Exception e) {
                log.debug("The account Id of the debtor account couldn't be saved: '{}'", e);
            }
            SignedJWT signedJWT = JwtUtil.getSignedJWT(consentDecisionRequest.getConsentJwt());
            decision.setScopes(
                    JwtUtil.getClaimValueMap(signedJWT, "scopes")
                            .values().stream().map(o -> (String) o).collect(Collectors.toList())
            );
            decision.setJwtClaimsSet(signedJWT.getJWTClaimsSet());
            String intentId = JwtUtil.getIdTokenClaim(signedJWT, INTENT_ID);
            log.debug("Intent Id from the requested claims '{}'", intentId);
            String clientId = JwtUtil.getClaimValue(signedJWT, CLIENT_ID);
            log.debug("Client Id from the JWT claims '{}'", clientId);
            String userId = JwtUtil.getClaimValue(signedJWT, USER_NAME);
            log.debug("User Id from the JWT claims '{}'", userId);
            decision.setResourceOwnerUsername(userId);
            decision.setIntentId(intentId);
            decision.setClientId(clientId);
            return decision;
        } catch (ParseException exception) {
            log.error("buildConsentDecision(consentDecision) Could not parse the consentJwt from consent decision object.", exception);
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
