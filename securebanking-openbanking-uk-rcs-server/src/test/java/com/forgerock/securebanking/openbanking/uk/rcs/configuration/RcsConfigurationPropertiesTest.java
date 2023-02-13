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
package com.forgerock.securebanking.openbanking.uk.rcs.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link RsConfiguration}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RsConfiguration.class, RsBackofficeConfiguration.class}, initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = RsBackofficeConfiguration.class)
@ActiveProfiles("test")
public class RcsConfigurationPropertiesTest {

    @Autowired
    private RsConfiguration configurationProperties;
    @Autowired
    private RsBackofficeConfiguration rsBackofficeConfiguration;

    @Test
    public void shouldHaveAllRCSProperties() {
        assertThat(configurationProperties.getBaseUri()).isNotNull();
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
