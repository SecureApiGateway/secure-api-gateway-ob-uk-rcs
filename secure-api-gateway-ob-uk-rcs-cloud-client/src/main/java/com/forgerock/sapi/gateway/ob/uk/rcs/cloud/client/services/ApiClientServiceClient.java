/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services;

import static org.springframework.http.HttpMethod.GET;

import java.net.URI;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants.URLParameters;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.configuration.CloudClientConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Specific service to retrieve the Api client data from the platform
 */
@Service
@Slf4j
public class ApiClientServiceClient extends BaseCloudClient {

    public ApiClientServiceClient(RestTemplate restTemplate, CloudClientConfiguration cloudClientConfiguration) {
        super(restTemplate, cloudClientConfiguration);
    }

    public ApiClient getApiClient(String apiClientId) throws ExceptionClient {
        final URI apiClientUri = cloudClientConfiguration.getApiClientUri()
                                                         .expand(Map.of(URLParameters.API_CLIENT_ID, apiClientId))
                                                         .toUri();

        log.debug("(ApiClientServiceClient#request) request the api client details from platform: {}", apiClientUri);
        try {
            ResponseEntity<ApiClient> responseEntity = restTemplate.exchange(apiClientUri, GET, createRequestEntity(),
                                                                             ApiClient.class);

            return responseEntity.getBody();
        } catch (RestClientException e) {
            throw createClientException(apiClientId, e);
        }
    }
}
