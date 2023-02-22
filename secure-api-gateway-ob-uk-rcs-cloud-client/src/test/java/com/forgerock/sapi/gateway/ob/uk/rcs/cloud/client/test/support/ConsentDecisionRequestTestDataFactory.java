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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support;

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.IntentType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDecisionRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDecisionRequestData;
import com.nimbusds.jwt.JWTClaimsSet;

import java.util.List;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance;
import static java.util.UUID.randomUUID;

/**
 * Test data factory for {@link ConsentClientDecisionRequest}.
 */
public class ConsentDecisionRequestTestDataFactory {

    // ACCOUNTS
    public static ConsentClientDecisionRequest aValidAccountConsentClientDecisionRequest() {
        return aValidAccountConsentClientDecisionRequestBuilder().build();
    }

    public static ConsentClientDecisionRequest aValidAccountConsentClientDecisionRequest(String intentId) {
        return aValidAccountConsentClientDecisionRequestBuilder(intentId).build();
    }

    public static ConsentClientDecisionRequest aValidAccountConsentClientDecisionRequest(String intentId, String clientId) {
        return aValidAccountConsentClientDecisionRequestBuilder(intentId, clientId).build();
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder aValidAccountConsentClientDecisionRequestBuilder() {
        return getAccountConsentClientDecisionRequestBuilder(IntentType.ACCOUNT_ACCESS_CONSENT);
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder aValidAccountConsentClientDecisionRequestBuilder(String intentId) {
        return getAccountConsentClientDecisionRequestBuilder(intentId);
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder aValidAccountConsentClientDecisionRequestBuilder(String intentId, String clientId) {
        return getAccountConsentClientDecisionRequestBuilder(intentId, clientId);
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder getAccountConsentClientDecisionRequestBuilder(IntentType intentType) {
        return ConsentClientDecisionRequest.builder()
                .intentId(intentType.generateIntentId())
                .consentJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                        "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .data(ConsentClientDecisionRequestData.builder()
                        .status(Constants.ConsentDecisionStatus.AUTHORISED)
                        .build())
                .clientId(randomUUID().toString())
                .accountIds(List.of(aValidFRAccountWithBalance().toString()))
                .jwtClaimsSet(new JWTClaimsSet.Builder().build())
                .resourceOwnerUsername(randomUUID().toString())
                .scopes(List.of("openid", "accounts"));
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder getAccountConsentClientDecisionRequestBuilder(String intentId) {
        return ConsentClientDecisionRequest.builder()
                .intentId(intentId)
                .consentJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                        "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .data(ConsentClientDecisionRequestData.builder()
                        .status(Constants.ConsentDecisionStatus.AUTHORISED)
                        .build())
                .clientId(randomUUID().toString())
                .accountIds(List.of(aValidFRAccountWithBalance().toString()))
                .jwtClaimsSet(new JWTClaimsSet.Builder().build())
                .resourceOwnerUsername(randomUUID().toString())
                .scopes(List.of("openid", "accounts"));
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder getAccountConsentClientDecisionRequestBuilder(String intentId, String clientId) {
        return ConsentClientDecisionRequest.builder()
                .intentId(intentId)
                .consentJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                        "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .data(ConsentClientDecisionRequestData.builder()
                        .status(Constants.ConsentDecisionStatus.AUTHORISED)
                        .build())
                .clientId(clientId)
                .accountIds(List.of(aValidFRAccountWithBalance().toString()))
                .jwtClaimsSet(new JWTClaimsSet.Builder().build())
                .resourceOwnerUsername(randomUUID().toString())
                .scopes(List.of("openid", "accounts"));
    }

    // DOMESTIC PAYMENTS
    public static ConsentClientDecisionRequest aValidDomesticPaymentConsentClientDecisionRequest() {
        return aValidDomesticPaymentConsentDecisionBuilder().build();
    }

    public static ConsentClientDecisionRequest aValidDomesticPaymentConsentClientDecisionRequest(String intentId) {
        return aValidAccountConsentClientDecisionRequestBuilder(intentId).build();
    }

    public static ConsentClientDecisionRequest aValidDomesticPaymentConsentClientDecisionRequest(String intentId, String clientId) {
        return aValidAccountConsentClientDecisionRequestBuilder(intentId, clientId).build();
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder aValidDomesticPaymentConsentDecisionBuilder() {
        return getDomesticPaymentConsentDecisionBuilder(IntentType.PAYMENT_DOMESTIC_CONSENT);
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder aValidDomesticPaymentConsentDecisionBuilder(String intentId) {
        return getAccountConsentClientDecisionRequestBuilder(intentId);
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder aValidDomesticPaymentConsentDecisionBuilder(String intentId, String clientId) {
        return getAccountConsentClientDecisionRequestBuilder(intentId, clientId);
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder getDomesticPaymentConsentDecisionBuilder(IntentType intentType) {
        return ConsentClientDecisionRequest.builder()
                .intentId(intentType.generateIntentId())
                .consentJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                        "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .data(ConsentClientDecisionRequestData.builder()
                        .status(Constants.ConsentDecisionStatus.AUTHORISED)
                        .build())
                .clientId(randomUUID().toString())
                .accountIds(List.of(aValidFRAccountWithBalance().toString()))
                .jwtClaimsSet(new JWTClaimsSet.Builder().build())
                .resourceOwnerUsername(randomUUID().toString())
                .scopes(List.of("openid", "payments"));
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder getDomesticPaymentConsentDecisionBuilder(String intentId) {
        return ConsentClientDecisionRequest.builder()
                .intentId(intentId)
                .consentJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                        "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .data(ConsentClientDecisionRequestData.builder()
                        .status(Constants.ConsentDecisionStatus.AUTHORISED)
                        .build())
                .clientId(randomUUID().toString())
                .accountIds(List.of(aValidFRAccountWithBalance().toString()))
                .jwtClaimsSet(new JWTClaimsSet.Builder().build())
                .resourceOwnerUsername(randomUUID().toString())
                .scopes(List.of("openid", "payments"));
    }

    private static ConsentClientDecisionRequest.ConsentClientDecisionRequestBuilder getDomesticPaymentConsentDecisionBuilder(String intentId, String clientId) {
        return ConsentClientDecisionRequest.builder()
                .intentId(intentId)
                .consentJwt("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                        "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .data(ConsentClientDecisionRequestData.builder()
                        .status(Constants.ConsentDecisionStatus.AUTHORISED)
                        .build())
                .clientId(clientId)
                .accountIds(List.of(aValidFRAccountWithBalance().toString()))
                .jwtClaimsSet(new JWTClaimsSet.Builder().build())
                .resourceOwnerUsername(randomUUID().toString())
                .scopes(List.of("openid", "payments"));
    }
}
