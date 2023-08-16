/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
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

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.TestApplicationClient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

/**
 * Unit test for {@link CloudClientConfiguration}
 */
@ContextConfiguration(classes = {CloudClientConfiguration.class}, initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = CloudClientConfiguration.class)
@ActiveProfiles("test")
@SpringBootTest(classes = TestApplicationClient.class, webEnvironment = MOCK)
public class CloudClientConfigurationTest {

    // values to get the proper context from the http verb that match with key mapping in the context, case-insensitive.
    private static final String GET = "GeT";
    private static final String PUT = "pUT";
    private static final String PATCH = "PaTCh";
    private static final String DELETE = "dElETe";
    @MockBean // mandatory to satisfied dependency for beans definitions
    private RestTemplate restTemplate;
    @Autowired
    private CloudClientConfiguration cloudClientConfiguration;

    @Test
    public void shouldConfigureIgBaseUri(){
        assertThat(cloudClientConfiguration.getBaseUri()).isEqualTo("http://ig:80");
    }

    @Test
    public void shouldHaveAllPropertiesSet() {
        assertThat(cloudClientConfiguration.getBaseUri()).isNotNull();
        assertThat(cloudClientConfiguration.getContextsApiClient()).isNotNull();
        assertThat(cloudClientConfiguration.getContextsUser()).isNotNull();
    }

    @Test
    public void shouldHaveApiClientContextVerbProperties() {
        assertThat(cloudClientConfiguration.getContextsApiClient().get(GET)).isNotNull();
        assertThat(cloudClientConfiguration.getContextsApiClient().get(PUT)).isNotNull();
        assertThat(cloudClientConfiguration.getContextsApiClient().get(PATCH)).isNotNull();
        assertThat(cloudClientConfiguration.getContextsApiClient().get(DELETE)).isNotNull();
    }

    @Test
    public void shouldHaveUserContextVerbProperties() {
        assertThat(cloudClientConfiguration.getContextsUser().get(GET)).isNotNull();
        assertThat(cloudClientConfiguration.getContextsUser().get(PUT)).isNotNull();
        assertThat(cloudClientConfiguration.getContextsUser().get(PATCH)).isNotNull();
        assertThat(cloudClientConfiguration.getContextsUser().get(DELETE)).isNotNull();
    }
}
