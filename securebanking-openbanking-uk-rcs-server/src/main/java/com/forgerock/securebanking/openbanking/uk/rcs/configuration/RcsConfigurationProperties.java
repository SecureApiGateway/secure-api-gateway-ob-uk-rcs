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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.net.URI;
import java.util.Map;

@Configuration
@Data
public class RcsConfigurationProperties {
    @Value("${rcs.issuerId}")
    private String issuerId;
    @Value("${rcs.rs_fqdn}")
    private String rsFqdn;
    @Value("${rcs.schema:https}")
    private String schema;

    private static final String _delimiter = "://";

    public String getRsFqdnURIAsString() {
        return String.join(_delimiter, schema, rsFqdn);
    }

    public URI getRsFqdnURI() {
        return URI.create(String.join(_delimiter, schema, rsFqdn));
    }
}
