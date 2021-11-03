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
package com.forgerock.securebanking.openbanking.uk.rcs.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class FilterConfigurationProperties {
    /*
    filters:
      cors:
        expected_origin: localhost # dev.forgerock.financial
        allowed_headers: accept-api-version, x-requested-with, authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN, Id-Token
        allowed_methods: GET, PUT, POST, DELETE, OPTIONS, PATCH
        allowed_credentials: true
        max_age: 3600
     */
    // Cors
    @Value("${filters.cors.expected_origin_ends_with}")
    private String expectedOriginEndsWith;
    @Value("${filters.cors.allowed_headers}")
    private String allowedHeaders;
    @Value("${filters.cors.allowed_methods}")
    private String allowedMethods;
    @Value("${filters.cors.allowed_credentials:true}")
    private boolean allowedCredentials;
    @Value("${filters.cors.max_age:3600}")
    private String maxAge;
}
