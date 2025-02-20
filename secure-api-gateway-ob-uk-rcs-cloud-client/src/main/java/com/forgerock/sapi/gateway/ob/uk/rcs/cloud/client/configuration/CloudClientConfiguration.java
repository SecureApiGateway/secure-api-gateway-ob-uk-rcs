/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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

import static org.springframework.util.StringUtils.hasText;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "cloud.client")
public class CloudClientConfiguration {

    private String baseUri;

    private String apiClientPath;

    private String usersPath;

    private UriComponents apiClientUri;

    private  UriComponents usersUri;

    @PostConstruct
    private void validateConfig() {
        if (!hasText(baseUri)) {
            throw new IllegalStateException("Required configuration: cloud.client.baseUri is missing");
        }
        if (!hasText(apiClientPath)) {
            throw new IllegalStateException("Required configuration: cloud.client.apiClientPath is missing");
        }
        if (!hasText(usersPath)) {
            throw new IllegalStateException("Required configuration: cloud.client.usersPath is missing");
        }

        apiClientUri = UriComponentsBuilder.fromUriString(baseUri).path(apiClientPath).encode().build();
        usersUri = UriComponentsBuilder.fromUriString(baseUri).path(usersPath).encode().build();
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void setApiClientPath(String apiClientPath) {
        this.apiClientPath = apiClientPath;
    }

    public void setUsersPath(String usersPath) {
        this.usersPath = usersPath;
    }

    public UriComponents getApiClientUri() {
        return apiClientUri;
    }

    public UriComponents getUsersUri() {
        return usersUri;
    }
}
