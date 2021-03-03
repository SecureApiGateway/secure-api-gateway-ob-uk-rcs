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
    private static final String SIGNING_URI = "api/crypto/";

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

    public String signClaims(String issuerId, JWTClaimsSet jwtClaimsSet, boolean includeKey) {
        return this.signClaims(restTemplate, null, issuerId, jwtClaimsSet.toString(), "signClaims", includeKey);
    }

    private String signClaims(RestTemplate restTemplate,
                              SigningRequest signingRequest,
                              String issuerId,
                              String payload,
                              String path,
                              boolean includeKey) {
        HttpHeaders headers = new HttpHeaders();
        if (issuerId != null) {
            headers.add("issuerId", issuerId);
        }

        if (includeKey) {
            headers.add("includeKey", "true");
        }

        if (signingRequest != null) {
            try {
                headers.add("signingRequest", objectMapper.writeValueAsString(signingRequest));
            } catch (JsonProcessingException var9) {
                log.error("Can't serialise signing request '{}' into a string", signingRequest, var9);
            }
        }

        HttpEntity<String> request = new HttpEntity<>(payload, headers);
        if (log.isDebugEnabled()) {
            log.debug("Sign claims {}", payload);
        }

        return restTemplate.postForObject(rcsProperties.getJwkmsBaseUrl() + SIGNING_URI + path, request, String.class);
    }
}
