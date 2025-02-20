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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "rs.api.resource")
@Data
public class RsResourceApiConfiguration {

    private Map<String, String> customerInfo = new LinkedCaseInsensitiveMap<>();

    public enum Operation {
        FIND_USER_BY_ID("findByUserId");
        private final String operation;

        Operation(String uriContext) {
            this.operation = uriContext;
        }

        public static Operation fromValue(String value) {
            for (Operation uriContext : Operation.values()) {
                if (uriContext.operation.equals(value)) {
                    return uriContext;
                }
            }
            throw new IllegalArgumentException("No enum constant '" + value + "'");
        }

        public String toString() {
            return operation;
        }
    }
}
