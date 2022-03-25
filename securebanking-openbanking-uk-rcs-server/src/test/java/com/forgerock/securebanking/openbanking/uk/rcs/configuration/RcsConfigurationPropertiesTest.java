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
package com.forgerock.securebanking.openbanking.uk.rcs.configuration;

import com.forgerock.securebanking.openbanking.uk.rcs.RcsApplicationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

/**
 * Unit test for {@link RcsConfigurationProperties}
 */
@ContextConfiguration(classes = {RcsConfigurationProperties.class})
@ActiveProfiles("test")
@SpringBootTest(classes = {RcsApplicationTestSupport.class}, webEnvironment = MOCK)
public class RcsConfigurationPropertiesTest {

    @Autowired
    private RcsConfigurationProperties configurationProperties;
    @Autowired
    private RsBackofficeConfiguration rsBackofficeConfiguration;

    @Test
    public void shouldHaveAllProperties() {
        assertThat(configurationProperties.getRsFqdn()).isNotNull();
        assertThat(configurationProperties.getIssuerId()).isNotNull();
        assertThat(configurationProperties.getSchema()).isNotNull();
    }

    @Test
    public void shouldHaveAllRSBackofficeProperties() {
        assertThat(rsBackofficeConfiguration.getAccounts().get(
                RsBackofficeConfiguration.UriContexts.FIND_USER_BY_ID.toString())
        ).isEqualTo("/backoffice/accounts/search/findByUserId");

        assertThat(rsBackofficeConfiguration.getDomesticPayments().get(
                RsBackofficeConfiguration.UriContexts.FIND_USER_BY_ID.toString())
        ).isEqualTo("/backoffice/domestic-payments/search/findByUserId");
    }
}
