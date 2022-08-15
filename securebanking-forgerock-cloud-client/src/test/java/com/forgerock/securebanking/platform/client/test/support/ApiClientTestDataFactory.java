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
package com.forgerock.securebanking.platform.client.test.support;

import com.forgerock.securebanking.platform.client.models.ApiClient;

import java.util.UUID;

/**
 * Test data factory for {@link ApiClient}
 */
public class ApiClientTestDataFactory {

    public static ApiClient aValidApiClient() {
        return aValidApiClient("c7303aee-2ff1-44b5-b21f-a7a3aaf39271");
    }

    public static ApiClient aValidApiClient(String oauth2ClientId) {
        return ApiClient.builder()
                .id(UUID.randomUUID().toString())
                .name("Test application")
                .officialName("Test application")
                .oauth2ClientId(oauth2ClientId)
                .logoUri("https://www.vhv.rs/dpng/d/455-4556963_warner-bros-logo-warner-brothers-logo-png-transparent.png")
                .build();
    }
}
