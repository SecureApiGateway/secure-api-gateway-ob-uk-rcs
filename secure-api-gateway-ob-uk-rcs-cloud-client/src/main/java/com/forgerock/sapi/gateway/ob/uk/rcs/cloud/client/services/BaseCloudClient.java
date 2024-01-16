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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services;

import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.configuration.CloudClientConfiguration;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBHeaders;
import com.forgerock.sapi.gateway.uk.common.shared.fapi.FapiInteractionIdContext;

public class BaseCloudClient {

    protected final RestTemplate restTemplate;
    protected final CloudClientConfiguration cloudClientConfiguration;

    public BaseCloudClient(RestTemplate restTemplate, CloudClientConfiguration cloudClientConfiguration) {
        this.restTemplate = restTemplate;
        this.cloudClientConfiguration = cloudClientConfiguration;
    }

    protected HttpEntity<?> createRequestEntity() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(OBHeaders.X_FAPI_INTERACTION_ID, FapiInteractionIdContext.getFapiInteractionId()
                                                                                 .orElseGet(() -> UUID.randomUUID().toString()));
        return new HttpEntity<>(httpHeaders);
    }
}
