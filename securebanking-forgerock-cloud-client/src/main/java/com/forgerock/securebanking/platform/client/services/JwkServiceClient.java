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
package com.forgerock.securebanking.platform.client.services;

import com.forgerock.securebanking.platform.client.configuration.ConfigurationPropertiesClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.base.Consent;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
@ComponentScan(basePackages = {"com.forgerock.securebanking.platform.client.configuration"})
public class JwkServiceClient {

    private final ConfigurationPropertiesClient configurationProperties;
    private final RestTemplate restTemplate;

    public JwkServiceClient(ConfigurationPropertiesClient configurationProperties, RestTemplate restTemplate) {
        this.configurationProperties = configurationProperties;
        this.restTemplate = restTemplate;
    }

    public String signClaims(Consent consent) throws ExceptionClient {
        String intentId = consent.getId();
        log.debug("signConsent() Received an request to signing the consent: '{}'", intentId);
        HttpEntity<Consent> requestEntity = new HttpEntity<>(consent, getHeaders());
        HttpMethod httpMethod = HttpMethod.resolve(configurationProperties.getJwkmsRequestMethod());
        return request(intentId, httpMethod, requestEntity);
    }

    public String signClaims(JWTClaimsSet jwtClaimsSet, String intentId) throws ExceptionClient {
        log.debug("signConsent() Received an request to signing the consent: '{}'", intentId);
        HttpEntity<Consent> requestEntity = new HttpEntity(jwtClaimsSet.toString(), getHeaders());
        HttpMethod httpMethod = HttpMethod.resolve(configurationProperties.getJwkmsRequestMethod());
        return request(intentId, httpMethod, requestEntity);
    }

    private String request(String intentId, HttpMethod httpMethod, HttpEntity httpEntity) throws ExceptionClient {
        String consentURL = configurationProperties.getIgServer() + configurationProperties.getJwkmsConsentSigningEndpoint();
        log.debug("request() request to signing the claims '{}' details from platform: {}", intentId, consentURL);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    consentURL,
                    httpMethod != null ? httpMethod : POST,
                    httpEntity,
                    String.class);

            return responseEntity != null ? responseEntity.getBody() : null;
        } catch (RestClientException e) {
            log.error(ErrorType.SERVER_ERROR.getDescription(), e);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.SERVER_ERROR)
                            .intentId(intentId)
                            .build(),
                    e.getMessage()
            );
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        // TODO - add additional required headers
        return headers;
    }
}
