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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.TestApplicationClient;

/**
 * Unit test for {@link CloudClientConfiguration}
 *
 * See application-test.yml for configuration values
 */
@ContextConfiguration(classes = {CloudClientConfiguration.class}, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("test")
@SpringBootTest(classes = TestApplicationClient.class, webEnvironment = MOCK)
public class CloudClientConfigurationTest {

    @MockBean // mandatory to satisfied dependency for beans definitions
    private RestTemplate restTemplate;
    @Autowired
    private CloudClientConfiguration cloudClientConfiguration;

    @Test
    public void testApiClientUri() {
        final String apiClientUri = cloudClientConfiguration.getApiClientUri()
                                                            .expand(Map.of("apiClientId", 123))
                                                            .toUriString();
        assertThat(apiClientUri).isEqualTo("http://ig:80/repo/apiclients/123");
    }

    @Test
    public void testUsersUri() {
        final String usersUri = cloudClientConfiguration.getUsersUri()
                                                        .expand(Map.of("userId", "user999"))
                                                        .toUriString();
        assertThat(usersUri).isEqualTo("http://ig:80/repo/users/user999");
    }
}
