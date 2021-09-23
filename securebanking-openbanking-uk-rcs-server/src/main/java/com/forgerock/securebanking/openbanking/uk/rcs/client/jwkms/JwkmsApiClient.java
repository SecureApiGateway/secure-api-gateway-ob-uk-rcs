/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.client.jwkms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.openbanking.uk.rcs.configuration.RcsConfigurationProperties;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class JwkmsApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RcsConfigurationProperties rcsProperties;

    public JwkmsApiClient(RestTemplate restTemplate,
                          ObjectMapper objectMapper,
                          RcsConfigurationProperties rcsProperties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.rcsProperties = rcsProperties;
    }

    public String signClaims(JWTClaimsSet jwtClaimsSet) {
        return this.signClaims(restTemplate, jwtClaimsSet.toString());
    }

    private String signClaims(RestTemplate restTemplate,
                              String payload) {
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<String> request = new HttpEntity<>(payload, headers);
        if (log.isDebugEnabled()) {
            log.debug("Sign claims {}", payload);
        }

        return restTemplate.postForObject(rcsProperties.getJwkmsBaseUrl() + rcsProperties.getJwkmsConsentSigningPath(), request, String.class);
    }
}
