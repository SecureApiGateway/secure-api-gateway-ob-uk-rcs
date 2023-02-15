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
package com.forgerock.securebanking.platform.client.configuration;

import com.forgerock.securebanking.platform.client.TestApplicationClient;
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
 * Unit test for {@link ConsentRepoConfiguration}
 */
@ContextConfiguration(classes = {ConsentRepoConfiguration.class}, initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = ConsentRepoConfiguration.class)
@ActiveProfiles("test")
@SpringBootTest(classes = TestApplicationClient.class, webEnvironment = MOCK)
public class ConsentRepoConfigurationTest {

    // values to get the proper context from the http verb that match with key mapping in the context, case-insensitive.
    private static final String GET = "GeT";
    private static final String PUT = "pUT";
    private static final String PATCH = "PaTCh";
    private static final String DELETE = "dElETe";
    @MockBean // mandatory to satisfied dependency for beans definitions
    private RestTemplate restTemplate;
    @Autowired
    private ConsentRepoConfiguration consentRepoConfiguration;

    @Test
    public void shouldConfigureIgBaseUri(){
        assertThat(consentRepoConfiguration.getConsentRepoBaseUri()).isEqualTo("http://ig:80");
    }

    @Test
    public void shouldHaveAllPropertiesSet() {
        assertThat(consentRepoConfiguration.getConsentRepoBaseUri()).isNotNull();
        assertThat(consentRepoConfiguration.getContextsRepoConsent()).isNotNull();
        assertThat(consentRepoConfiguration.getContextsApiClient()).isNotNull();
        assertThat(consentRepoConfiguration.getContextsUser()).isNotNull();
        assertThat(consentRepoConfiguration.getScheme()).isNotNull();
        assertThat(consentRepoConfiguration.getScheme()).isEqualTo("http");
    }

    @Test
    public void shouldHaveConsentContextVerbProperties() {
        assertThat(consentRepoConfiguration.getContextsRepoConsent().get(GET)).isNotNull();
        assertThat(consentRepoConfiguration.getContextsRepoConsent().get(PUT)).isNotNull();
        assertThat(consentRepoConfiguration.getContextsRepoConsent().get(PATCH)).isNotNull();
        assertThat(consentRepoConfiguration.getContextsRepoConsent().get(DELETE)).isNotNull();
    }

    @Test
    public void shouldHaveApiClientContextVerbProperties() {
        assertThat(consentRepoConfiguration.getContextsApiClient().get(GET)).isNotNull();
        assertThat(consentRepoConfiguration.getContextsApiClient().get(PUT)).isNotNull();
        assertThat(consentRepoConfiguration.getContextsApiClient().get(PATCH)).isNotNull();
        assertThat(consentRepoConfiguration.getContextsApiClient().get(DELETE)).isNotNull();
    }

    @Test
    public void shouldHaveUserContextVerbProperties() {
        assertThat(consentRepoConfiguration.getContextsUser().get(GET)).isNotNull();
        assertThat(consentRepoConfiguration.getContextsUser().get(PUT)).isNotNull();
        assertThat(consentRepoConfiguration.getContextsUser().get(PATCH)).isNotNull();
        assertThat(consentRepoConfiguration.getContextsUser().get(DELETE)).isNotNull();
    }
}
