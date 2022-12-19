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
package com.forgerock.securebanking.openbanking.uk.rcs.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "rs.backoffice.uris")
@Data
public class RsBackofficeConfiguration {
    private Map<String, String> accounts = new LinkedCaseInsensitiveMap<>();
    private Map<String, String> domesticPayments = new LinkedCaseInsensitiveMap<>();

    public enum UriContexts {
        FIND_USER_BY_ID("findUserById"),
        FIND_BY_ACCOUNT_IDENTIFIERS("findByAccountIdentifiers");
        private final String uriContext;

        UriContexts(String uriContext) {
            this.uriContext = uriContext;
        }

        public static UriContexts fromValue(String value) {
            for (UriContexts uriContext : UriContexts.values()) {
                if (uriContext.uriContext.equals(value)) {
                    return uriContext;
                }
            }
            throw new IllegalArgumentException("No enum constant '" + value + "'");
        }

        public String toString() {
            return uriContext;
        }
    }
}
