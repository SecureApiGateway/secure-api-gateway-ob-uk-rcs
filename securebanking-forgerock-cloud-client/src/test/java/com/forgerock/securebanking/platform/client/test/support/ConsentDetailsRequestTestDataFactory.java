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
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentRequest;
import com.forgerock.securebanking.platform.client.models.general.ConsentRequest;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentRequest;
import com.forgerock.securebanking.platform.client.models.general.User;
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
    public static AccountConsentRequest aValidAccountConsentDetailsRequest() {
        return aValidAccountConsentDetailsRequestBuilder().build();
    }

    public static AccountConsentRequest aValidAccountConsentDetailsRequest(String intentId) {
        return aValidAccountConsentDetailsRequestBuilder(intentId).build();
    }

    public static AccountConsentRequest aValidAccountConsentDetailsRequest(String intentId, User user) {
        return aValidAccountConsentDetailsRequestBuilder(intentId, user).build();
    }

    private static AccountConsentRequest.AccountConsentRequestBuilder aValidAccountConsentDetailsRequestBuilder() {
        return getAccountConsentDetailsRequestBuilder(IntentType.ACCOUNT_ACCESS_CONSENT);
    }

    private static AccountConsentRequest.AccountConsentRequestBuilder aValidAccountConsentDetailsRequestBuilder(String intentId) {
        return getAccountConsentDetailsRequestBuilder(intentId);
    }

    private static AccountConsentRequest.AccountConsentRequestBuilder aValidAccountConsentDetailsRequestBuilder(String intentId, User user) {
        return getAccountConsentDetailsRequestBuilder(intentId, user);
    }

    private static AccountConsentRequest.AccountConsentRequestBuilder getAccountConsentDetailsRequestBuilder(IntentType intentType) {
        try {
            return AccountConsentRequest.builder()
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

    private static AccountConsentRequest.AccountConsentRequestBuilder getAccountConsentDetailsRequestBuilder(String intentId) {
        try {
            return AccountConsentRequest.builder()
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

    private static AccountConsentRequest.AccountConsentRequestBuilder getAccountConsentDetailsRequestBuilder(String intentId, User user) {
        try {
            return AccountConsentRequest.builder()
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

    // PAYMENTS
    public static DomesticPaymentConsentRequest aValidDomesticPaymentConsentDetailsRequest() {
        return aValidDomesticPaymentConsentDetailsRequestBuilder().build();
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder aValidDomesticPaymentConsentDetailsRequestBuilder() {
        return getDomesticPaymentConsentDetailsRequestBuilder(IntentType.PAYMENT_DOMESTIC_CONSENT);
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder aValidDomesticScheduledPaymentConsentDetailsRequestBuilder() {
        return getDomesticPaymentConsentDetailsRequestBuilder(IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT);
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder aValidDomesticStandingOrderConsentDetailsRequestBuilder() {
        return getDomesticPaymentConsentDetailsRequestBuilder(IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT);
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder aValidInternationalPaymentConsentDetailsRequestBuilder() {
        return getDomesticPaymentConsentDetailsRequestBuilder(IntentType.PAYMENT_INTERNATIONAL_CONSENT);
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder aValidInternationalScheduledPaymentConsentDetailsRequestBuilder() {
        return getDomesticPaymentConsentDetailsRequestBuilder(IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT);
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder aValidInternationalStandingOrderConsentDetailsRequestBuilder() {
        return getDomesticPaymentConsentDetailsRequestBuilder(IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT);
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder aValidFilePaymentConsentDetailsRequestBuilder() {
        return getDomesticPaymentConsentDetailsRequestBuilder(IntentType.PAYMENT_FILE_CONSENT);
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder aValidFundsConfirmationConsentDetailsRequestBuilder() {
        return getDomesticPaymentConsentDetailsRequestBuilder(IntentType.FUNDS_CONFIRMATION_CONSENT);
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder getDomesticPaymentConsentDetailsRequestBuilder(IntentType intentType) {
        try {
            return DomesticPaymentConsentRequest.builder()
                    .intentId(intentType.getIntentIdPrefix() + randomUUID())
                    .consentRequestJwt(SignedJWT.parse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                    .user(UserTestDataFactory.aValidUser(randomUUID().toString(), "testUserName"))
                    .clientId(randomUUID().toString());
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid Signed JWT value");
        }
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder getDomesticPaymentConsentDetailsRequestBuilder(String intentId) {
        try {
            return DomesticPaymentConsentRequest.builder()
                    .intentId(intentId)
                    .consentRequestJwt(SignedJWT.parse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                    .user(UserTestDataFactory.aValidUser(randomUUID().toString(), "testUserName"))
                    .clientId(randomUUID().toString());
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid Signed JWT value");
        }
    }

    private static DomesticPaymentConsentRequest.DomesticPaymentConsentRequestBuilder getDomesticPaymentConsentDetailsRequestBuilder(String intentId, User user) {
        try {
            return DomesticPaymentConsentRequest.builder()
                    .intentId(intentId)
                    .consentRequestJwt(SignedJWT.parse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                    .user(user)
                    .clientId(randomUUID().toString());
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid Signed JWT value");
        }
    }
}
