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

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.IntentType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;

import static java.util.UUID.randomUUID;

/**
 * Test data factory for {@link ConsentClientDetailsRequest}.
 */
public class ConsentDetailsRequestTestDataFactory {

    public static ConsentClientDetailsRequest aValidConsentDetailsRequest(IntentType intentType) {
        try {
            return ConsentClientDetailsRequest.builder()
                    .intentId(intentType.getIntentIdPrefix() + randomUUID())
                    .consentRequestJwt(SignedJWT.parse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                    .user(UserTestDataFactory.aValidUser(randomUUID().toString(), "testUserName"))
                    .clientId(randomUUID().toString())
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid Signed JWT value");
        }
    }

    public static ConsentClientDetailsRequest aValidConsentDetailsRequest(String intentId) {
        try {
            return ConsentClientDetailsRequest.builder()
                    .intentId(intentId)
                    .consentRequestJwt(SignedJWT.parse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                    .user(UserTestDataFactory.aValidUser(randomUUID().toString(), "testUserName"))
                    .clientId(randomUUID().toString())
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid Signed JWT value");
        }
    }

    public static ConsentClientDetailsRequest aValidConsentDetailsRequest(String intentId, User user) {
        try {
            return ConsentClientDetailsRequest.builder()
                    .intentId(intentId)
                    .consentRequestJwt(SignedJWT.parse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                    .user(user)
                    .clientId(randomUUID().toString())
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid Signed JWT value");
        }
    }
}
