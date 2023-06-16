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
package com.forgerock.sapi.gateway.rcs.conent.store.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.google.common.annotations.VisibleForTesting;

@Configuration
@ComponentScan(basePackageClasses = ConsentStoreClientConfiguration.class)
public class ConsentStoreClientConfiguration {

    @Value("${rcs.consent.store.api.baseUri}")
    private String baseUri;

    public String getBaseUri() {
        return baseUri;
    }

    @VisibleForTesting
    void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
}