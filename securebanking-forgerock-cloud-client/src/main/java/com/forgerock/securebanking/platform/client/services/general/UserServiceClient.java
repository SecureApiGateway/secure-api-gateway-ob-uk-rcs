/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.securebanking.platform.client.services.general;

import com.forgerock.securebanking.platform.client.Constants;
import com.forgerock.securebanking.platform.client.configuration.ConfigurationPropertiesClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorClient;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.general.User;
import com.forgerock.securebanking.platform.client.utils.url.UrlContext;
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
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final ConfigurationPropertiesClient configurationProperties;

    public UserServiceClient(RestTemplate restTemplate, ConfigurationPropertiesClient configurationProperties) {
        this.restTemplate = restTemplate;
        this.configurationProperties = configurationProperties;
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
        String userURL = configurationProperties.getIgServer() +
                UrlContext.replaceParameterContextValue(
                        configurationProperties.getContextsUser().get(GET.name()),
                        Constants.URLParameters.USER_ID,
                        userId
                );
        log.debug("(UserServiceClient#request) request the user details from platform: {}", userURL);
        try {
            ResponseEntity<User> responseEntity = restTemplate.exchange(
                    userURL,
                    GET,
                    null,
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
