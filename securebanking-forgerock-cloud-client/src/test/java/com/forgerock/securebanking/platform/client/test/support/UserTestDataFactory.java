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

import com.forgerock.securebanking.platform.client.models.general.User;

/**
 * Test data factory for {@link User}
 */
public class UserTestDataFactory {

    public static User aValidUser() {
        return aValidUser("c7303aee-2ff1-44b5-b21f-a7a3aaf39271");
    }

    public static User aValidUser(String userId) {
        return User.builder()
                .id(userId)
                .accountStatus("active")
                .userName("testUserName")
                .givenName("testName")
                .surname("test Surname")
                .mail("someemail@no.com")
                .build();
    }

    public static User aValidUser(String userId, String userName) {
        return User.builder()
                .id(userId)
                .accountStatus("active")
                .userName("testUserName")
                .givenName("testName")
                .surname("test Surname")
                .mail("someemail@no.com")
                .build();
    }
}
