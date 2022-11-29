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
package com.forgerock.securebanking.openbanking.uk.rcs.configuration.swagger;

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
 * Unit test for {@link SwaggerConfiguration}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SwaggerConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = SwaggerConfiguration.class)
@ActiveProfiles("test")
public class SwaggerConfigurationTest {

    @Autowired
    private SwaggerConfiguration swaggerConfiguration;

    @Test
    public void haveProperties(){
        assertThat(swaggerConfiguration.getTitle()).isNotNull();
        assertThat(swaggerConfiguration.getDescription()).isNotNull();
        assertThat(swaggerConfiguration.getLicense()).isNotNull();
        assertThat(swaggerConfiguration.getLicenseUrl()).isNotNull();
        assertThat(swaggerConfiguration.getTermsOfServiceUrl()).isNotNull();
        assertThat(swaggerConfiguration.getContactName()).isNotNull();
        assertThat(swaggerConfiguration.getContactUrl()).isNotNull();
        assertThat(swaggerConfiguration.getDocketApisBasePackage()).isNotNull();
        assertThat(swaggerConfiguration.getDocketPathsSelectorRegex()).isNotNull();
    }
}