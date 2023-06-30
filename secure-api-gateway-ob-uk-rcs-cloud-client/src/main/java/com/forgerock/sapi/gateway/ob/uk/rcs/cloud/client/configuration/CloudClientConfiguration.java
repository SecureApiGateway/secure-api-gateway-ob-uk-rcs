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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.configuration;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Map;

import javax.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "cloud.client")
@Data
public class CloudClientConfiguration {

    private String baseUri;

    /*
     * Spring maps the properties, the keys from file will be the map keys
     * @see: application-test.yml
     * case-insensitive map's keys
     * @Use pattern: contextMap.get(http-verb-any-case)
     * @Use: contextAccountsConsent.get("GeT")
     */
    private Map<String, String> contextsApiClient = new LinkedCaseInsensitiveMap<>();
    private Map<String, String> contextsUser = new LinkedCaseInsensitiveMap<>();

    @PostConstruct
    private void validateConfig() {
        if (baseUri == null || baseUri.isBlank()) {
            throw new IllegalStateException("Required configuration: cloud.client.baseUri is missing");
        }
        if (contextsApiClient.isEmpty()) {
            throw new IllegalStateException("Required configuration: cloud.client.contextsApiClient map is missing");
        }
        if (contextsUser.isEmpty()) {
            throw new IllegalStateException("Required configuration: cloud.client.contextsUser map is missing");
        }
    }

}