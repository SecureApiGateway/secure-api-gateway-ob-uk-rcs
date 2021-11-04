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

import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.models.ConsentRequest;
import com.forgerock.securebanking.platform.client.models.User;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.List;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance;
import static java.util.UUID.randomUUID;

/**
 * Test data factory for {@link ConsentRequest}.
 */
public class ConsentDetailsRequestTestDataFactory {

    // ACCOUNTS
    public static ConsentRequest aValidAccountConsentDetailsRequest() {
        return aValidAccountConsentDetailsRequestBuilder().build();
    }

    public static ConsentRequest aValidAccountConsentDetailsRequest(String intentId) {
        return aValidAccountConsentDetailsRequestBuilder(intentId).build();
    }

    public static ConsentRequest aValidAccountConsentDetailsRequest(String intentId, User user) {
        return aValidAccountConsentDetailsRequestBuilder(intentId, user).build();
    }

    private static ConsentRequest.ConsentRequestBuilder aValidAccountConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder(IntentType.ACCOUNT_ACCESS_CONSENT);
    }

    private static ConsentRequest.ConsentRequestBuilder aValidAccountConsentDetailsRequestBuilder(String intentId) {
        return getConsentDetailsRequestBuilder(intentId);
    }

    private static ConsentRequest.ConsentRequestBuilder aValidAccountConsentDetailsRequestBuilder(String intentId, User user) {
        return getConsentDetailsRequestBuilder(intentId, user);
    }

    // PAYMENTS
    public static ConsentRequest aValidDomesticPaymentConsentDetailsRequest() {
        return aValidDomesticPaymentConsentDetailsRequestBuilder().build();
    }

    private static ConsentRequest.ConsentRequestBuilder aValidDomesticPaymentConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder(IntentType.PAYMENT_DOMESTIC_CONSENT);
    }

    private static ConsentRequest.ConsentRequestBuilder aValidDomesticScheduledPaymentConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder(IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT);
    }

    private static ConsentRequest.ConsentRequestBuilder aValidDomesticStandingOrderConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder(IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT);
    }

    private static ConsentRequest.ConsentRequestBuilder aValidInternationalPaymentConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder(IntentType.PAYMENT_INTERNATIONAL_CONSENT);
    }

    private static ConsentRequest.ConsentRequestBuilder aValidInternationalScheduledPaymentConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder(IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT);
    }

    private static ConsentRequest.ConsentRequestBuilder aValidInternationalStandingOrderConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder(IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT);
    }

    private static ConsentRequest.ConsentRequestBuilder aValidFilePaymentConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder(IntentType.PAYMENT_FILE_CONSENT);
    }

    private static ConsentRequest.ConsentRequestBuilder aValidFundsConfirmationConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder(IntentType.FUNDS_CONFIRMATION_CONSENT);
    }

    private static ConsentRequest.ConsentRequestBuilder getConsentDetailsRequestBuilder(IntentType intentType) {
        try {
            return ConsentRequest.builder()
                    .intentId(intentType.getIntentIdPrefix() + randomUUID())
                    .consentRequestJwt(SignedJWT.parse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                    .accounts(List.of(aValidFRAccountWithBalance()))
                    .user(UserTestDataFactory.aValidUser(randomUUID().toString(), "testUserName"))
                    .clientId(randomUUID().toString());
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid Signed JWT value");
        }
    }

    private static ConsentRequest.ConsentRequestBuilder getConsentDetailsRequestBuilder(String intentId) {
        try {
            return ConsentRequest.builder()
                    .intentId(intentId)
                    .consentRequestJwt(SignedJWT.parse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                    .accounts(List.of(aValidFRAccountWithBalance()))
                    .user(UserTestDataFactory.aValidUser(randomUUID().toString(), "testUserName"))
                    .clientId(randomUUID().toString());
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid Signed JWT value");
        }
    }

    private static ConsentRequest.ConsentRequestBuilder getConsentDetailsRequestBuilder(String intentId, User user) {
        try {
            return ConsentRequest.builder()
                    .intentId(intentId)
                    .consentRequestJwt(SignedJWT.parse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                    .accounts(List.of(aValidFRAccountWithBalance()))
                    .user(user)
                    .clientId(randomUUID().toString());
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid Signed JWT value");
        }
    }
}