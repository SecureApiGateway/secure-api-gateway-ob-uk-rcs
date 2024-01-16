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
package com.forgerock.sapi.gateway.rcs.consent.store.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBHeaders;
import com.forgerock.sapi.gateway.uk.common.shared.fapi.FapiInteractionIdContext;

@ExtendWith(MockitoExtension.class)
class BaseRestConsentStoreClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private ObjectMapper objectMapper;

    private BaseRestConsentStoreClient baseRestConsentStoreClient;

    @BeforeEach
    public void beforeEach() {
        baseRestConsentStoreClient = new BaseRestConsentStoreClient(restTemplateBuilder, objectMapper) {};
        FapiInteractionIdContext.removeFapiInteractionId();
    }

    @Test
    void testCreatingHeadersWithRandomFapiInteractionId() {
        final String apiClientId = "client-1234";
        final HttpHeaders headers = baseRestConsentStoreClient.createHeaders(apiClientId);
        assertThat(headers.get("x-api-client-id")).isEqualTo(List.of(apiClientId));
        assertThat(headers.get(OBHeaders.X_FAPI_INTERACTION_ID)).hasSize(1).first().isNotNull();
    }

    @Test
    void testCreatingHeadersWithFapiInteractionIdFromContext() {
        final String fapiInteractionId = "fapi-id-1243";
        FapiInteractionIdContext.setFapiInteractionId(fapiInteractionId);

        final String apiClientId = "client-1234";
        final HttpHeaders headers = baseRestConsentStoreClient.createHeaders(apiClientId);
        assertThat(headers.get("x-api-client-id")).isEqualTo(List.of(apiClientId));
        assertThat(headers.get(OBHeaders.X_FAPI_INTERACTION_ID)).hasSize(1).first().isEqualTo(fapiInteractionId);
    }

}