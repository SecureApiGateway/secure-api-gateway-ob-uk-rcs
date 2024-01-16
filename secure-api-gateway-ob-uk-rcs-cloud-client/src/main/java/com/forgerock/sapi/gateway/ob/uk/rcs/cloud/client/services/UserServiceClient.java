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

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.configuration.CloudClientConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.utils.url.UrlContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.GET;

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
        User user = request(userId);
        if (user == null) {
            String message = String.format("UserId '%s' not found.", userId);
            log.error(message);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.NOT_FOUND)
                            .userId(userId)
                            .build(),
                    message
            );
        }
        return user;
    }

    private User request(String userId) throws ExceptionClient {
        String userURL = cloudClientConfiguration.getBaseUri() +
                UrlContext.replaceParameterContextValue(
                        cloudClientConfiguration.getContextsUser().get(GET.name()),
                        Constants.URLParameters.USER_ID,
                        userId
                );
        log.debug("(UserServiceClient#request) request the user details from platform: {}", userURL);
        try {
            ResponseEntity<User> responseEntity = restTemplate.exchange(
                    userURL,
                    GET,
                    createRequestEntity(),
                    User.class);
            return responseEntity != null ? responseEntity.getBody() : null;
        } catch (RestClientException e) {
            log.error(ErrorType.SERVER_ERROR.getDescription(), e);
            throw new ExceptionClient(
                    ErrorClient.builder()
                            .errorType(ErrorType.SERVER_ERROR)
                            .userId(userId)
                            .build(),
                    e.getMessage()
            );
        }
    }
}
