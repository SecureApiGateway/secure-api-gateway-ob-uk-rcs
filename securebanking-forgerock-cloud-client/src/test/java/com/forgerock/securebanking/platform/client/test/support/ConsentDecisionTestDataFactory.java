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
package com.forgerock.securebanking.platform.client.test.support;

import com.forgerock.securebanking.platform.client.Constants;
import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.models.AccountConsentDecision;
import com.forgerock.securebanking.platform.client.models.ConsentDecision;
import com.forgerock.securebanking.platform.client.models.ConsentDecisionData;
import com.nimbusds.jwt.JWTClaimsSet;

import java.util.List;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance;
import static java.util.UUID.randomUUID;

/**
 * Test data factory for {@link ConsentDecision}.
 */
public class ConsentDecisionTestDataFactory {

    // ACCOUNTS
    public static ConsentDecision aValidAccountConsentDecision() {
        return aValidAccountConsentDecisionBuilder().build();
    }

    public static ConsentDecision aValidAccountConsentDecision(String intentId) {
        return aValidAccountConsentDecisionBuilder(intentId).build();
    }

    public static ConsentDecision aValidAccountConsentDecision(String intentId, String clientId) {
        return aValidAccountConsentDecisionBuilder(intentId, clientId).build();
    }

    private static ConsentDecision.ConsentDecisionBuilder aValidAccountConsentDecisionBuilder() {
        return getConsentDecisionBuilder(IntentType.ACCOUNT_ACCESS_CONSENT);
    }

    private static ConsentDecision.ConsentDecisionBuilder aValidAccountConsentDecisionBuilder(String intentId) {
        return getConsentDecisionBuilder(intentId);
    }

    private static ConsentDecision.ConsentDecisionBuilder aValidAccountConsentDecisionBuilder(String intentId, String clientId) {
        return getConsentDecisionBuilder(intentId, clientId);
    }

    // TODO: PAYMENTS

    private static ConsentDecision.ConsentDecisionBuilder getConsentDecisionBuilder(IntentType intentType) {
        return AccountConsentDecision.builder()
                .intentId(intentType.generateIntentId())
                .consentJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                        "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .data(ConsentDecisionData.builder()
                        .status(Constants.ConsentDecision.APPROVED)
                        .build())
                .clientId(randomUUID().toString())
                .accountIds(List.of(aValidFRAccountWithBalance().toString()))
                .jwtClaimsSet(new JWTClaimsSet.Builder().build())
                .resourceOwnerUsername(randomUUID().toString())
                .scopes(List.of("openid", "accounts"));
    }

    private static ConsentDecision.ConsentDecisionBuilder getConsentDecisionBuilder(String intentId) {
        return AccountConsentDecision.builder()
                .intentId(intentId)
                .consentJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                        "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .data(ConsentDecisionData.builder()
                        .status(Constants.ConsentDecision.APPROVED)
                        .build())
                .clientId(randomUUID().toString())
                .accountIds(List.of(aValidFRAccountWithBalance().toString()))
                .jwtClaimsSet(new JWTClaimsSet.Builder().build())
                .resourceOwnerUsername(randomUUID().toString())
                .scopes(List.of("openid", "accounts"));
    }

    private static ConsentDecision.ConsentDecisionBuilder getConsentDecisionBuilder(String intentId, String clientId) {
        return AccountConsentDecision.builder()
                .intentId(intentId)
                .consentJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                        "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .data(ConsentDecisionData.builder()
                        .status(Constants.ConsentDecision.APPROVED)
                        .build())
                .clientId(clientId)
                .accountIds(List.of(aValidFRAccountWithBalance().toString()))
                .jwtClaimsSet(new JWTClaimsSet.Builder().build())
                .resourceOwnerUsername(randomUUID().toString())
                .scopes(List.of("openid", "accounts"));
    }
}