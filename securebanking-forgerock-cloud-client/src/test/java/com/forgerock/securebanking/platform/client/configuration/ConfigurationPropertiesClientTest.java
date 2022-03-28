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
package com.forgerock.securebanking.platform.client.configuration;

import com.forgerock.securebanking.platform.client.TestApplicationClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

/**
 * Unit test for {@link ConfigurationPropertiesClient}
 */
@ContextConfiguration(classes = ConfigurationPropertiesClient.class)
@ActiveProfiles("test")
@SpringBootTest(classes = TestApplicationClient.class, webEnvironment = MOCK)
public class ConfigurationPropertiesClientTest {

    // values to get the proper context from the http verb that match with key mapping in the context, case-insensitive.
    private static final String GET = "GeT";
    private static final String PUT = "pUT";
    private static final String PATCH = "PaTCh";
    private static final String DELETE = "dElETe";
    private static final String EXPECTED_JWKMS_CONSENT_SIGNING_ENDPOINT = "/jwkms/rcs/signresponse";
    private static final String EXPECTED_JWKMS_REQUEST_METHOD = "POST";
    private static final String EXPECTED_JWK_URI = "https://iam.dev.forgerock.financial/am/oauth2/connect/jwk_uri";
    @MockBean // mandatory to satisfied dependency for beans definitions
    private RestTemplate restTemplate;
    @Autowired
    private ConfigurationPropertiesClient configurationPropertiesClient;

    @Test
    public void shouldHaveAllPropertiesSet() {
        assertThat(configurationPropertiesClient.getIgFqdnURIAsString()).isNotNull();
        assertThat(configurationPropertiesClient.getIdentityPlatformFqdn()).isNotNull();
        assertThat(configurationPropertiesClient.getContextsAccountsConsent()).isNotNull();
        assertThat(configurationPropertiesClient.getContextsDomesticPaymentConsent()).isNotNull();
        assertThat(configurationPropertiesClient.getContextsApiClient()).isNotNull();
        assertThat(configurationPropertiesClient.getContextsUser()).isNotNull();
        assertThat(configurationPropertiesClient.getJwkmsConsentSigningEndpoint()).isNotNull();
        assertThat(configurationPropertiesClient.getJwkmsRequestMethod()).isNotNull();
        assertThat(configurationPropertiesClient.getJwkUri()).isNotNull();
        assertThat(configurationPropertiesClient.getScheme()).isNotNull();
        assertThat(configurationPropertiesClient.getScheme()).isEqualTo("https");
    }

    @Test
    public void shouldHaveAccountContextVerbProperties() {
        assertThat(configurationPropertiesClient.getContextsAccountsConsent().get(GET)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsAccountsConsent().get(PUT)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsAccountsConsent().get(PATCH)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsAccountsConsent().get(DELETE)).isNotNull();
    }

    @Test
    public void shouldHavePaymentContextVerbProperties() {
        assertThat(configurationPropertiesClient.getContextsDomesticPaymentConsent().get(GET)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsDomesticPaymentConsent().get(PUT)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsDomesticPaymentConsent().get(PATCH)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsDomesticPaymentConsent().get(DELETE)).isNotNull();
    }

    @Test
    public void shouldHaveApiClientContextVerbProperties() {
        assertThat(configurationPropertiesClient.getContextsApiClient().get(GET)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsApiClient().get(PUT)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsApiClient().get(PATCH)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsApiClient().get(DELETE)).isNotNull();
    }

    @Test
    public void shouldHaveUserContextVerbProperties() {
        assertThat(configurationPropertiesClient.getContextsUser().get(GET)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsUser().get(PUT)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsUser().get(PATCH)).isNotNull();
        assertThat(configurationPropertiesClient.getContextsUser().get(DELETE)).isNotNull();
    }

    @Test
    public void shouldHaveJwkmsProperties() {
        assertThat(configurationPropertiesClient.getJwkmsConsentSigningEndpoint()).isEqualTo(EXPECTED_JWKMS_CONSENT_SIGNING_ENDPOINT);
        assertThat(configurationPropertiesClient.getJwkmsRequestMethod()).isEqualTo(EXPECTED_JWKMS_REQUEST_METHOD);
    }

    @Test
    public void shouldHaveJwkUriProperty() {
        assertThat(configurationPropertiesClient.getJwkUri()).isEqualTo(EXPECTED_JWK_URI);
    }
}
