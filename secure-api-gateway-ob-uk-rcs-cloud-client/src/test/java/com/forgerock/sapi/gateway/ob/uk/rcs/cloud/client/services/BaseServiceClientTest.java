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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services;

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.configuration.ConsentRepoConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.utils.url.UrlContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class BaseServiceClientTest {
    @Mock
    protected ConsentRepoConfiguration configurationProperties;

    @Mock
    protected RestTemplate restTemplate;

    protected MockedStatic<UrlContext> urlContextMockedStatic;

    @BeforeEach
    public void setup() {
        urlContextMockedStatic = Mockito.mockStatic(UrlContext.class);
        urlContextMockedStatic.when(
                () -> UrlContext.replaceParameterContextIntentId(anyString(), anyString())
        ).thenReturn("http://a.domain/context/intent-id-xxxx");
    }

    @AfterEach
    public void close() {
        urlContextMockedStatic.close();
    }

}
