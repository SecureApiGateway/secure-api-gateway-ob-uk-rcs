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
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;

import lombok.extern.slf4j.Slf4j;

/**
 * Specific service to retrieve the user data from the platform
 */
@Service
@Slf4j
public class UserServiceClient extends BaseCloudClient {


    public UserServiceClient(RestTemplate restTemplate, CloudClientConfiguration cloudClientConfiguration) {
        super(restTemplate, cloudClientConfiguration);
    }

    public User getUser(String userId) throws ExceptionClient {
        final URI userUri = cloudClientConfiguration.getUsersUri()
                                                    .expand(Map.of(URLParameters.USER_ID, userId))
                                                    .toUri();
        log.debug("(UserServiceClient#request) request the user details from platform: {}", userUri);
        try {
            ResponseEntity<User> responseEntity = restTemplate.exchange(
                    userUri,
                    GET,
                    createRequestEntity(),
                    User.class);
            return responseEntity.getBody();
        } catch (RestClientException e) {
            throw createClientException(userId, e);
        }
    }

}
