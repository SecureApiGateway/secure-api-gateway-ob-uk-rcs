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
package com.forgerock.securebanking.openbanking.uk.rcs.client.am;

import com.forgerock.securebanking.openbanking.uk.rcs.configuration.AmConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Retrieves user profile related information from AM.
 */
@Service
@Slf4j
public class UserProfileService {

    private final RestTemplate restTemplate;
    private final AmConfigurationProperties amConfiguration;

    public UserProfileService(RestTemplate restTemplate, AmConfigurationProperties amConfiguration) {
        this.restTemplate = restTemplate;
        this.amConfiguration = amConfiguration;
    }

    /**
     * Retrieves the username from AM, using the logged in user's ssoToken.
     *
     * @param ssoToken The logged in user's session cookie.
     * @return The username value
     */
    public String getUsername(String ssoToken) {
        return getProfile(ssoToken).get(amConfiguration.getUserProfileId());
    }

    private Map<String, String> getProfile(String ssoToken) {
        log.info("Get user profile behind the sso token");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        // TODO - ideally we shouldn't be sending the session cookie to AM
        headers.add("Cookie", amConfiguration.getCookieName() + "=" + ssoToken);
        headers.add("Accept-API-Version", "protocol=1.0,resource=1.0");
        HttpEntity<Map<String, String>> request = new HttpEntity(headers);
        log.debug("Send user info request to the AS {}", amConfiguration.getUserProfileEndpoint());
        // TODO - deserialize this to an object, rather than just Map
        return (Map)this.restTemplate.postForObject(amConfiguration.getUserProfileEndpoint(), request, Map.class, new Object[0]);
    }
}
