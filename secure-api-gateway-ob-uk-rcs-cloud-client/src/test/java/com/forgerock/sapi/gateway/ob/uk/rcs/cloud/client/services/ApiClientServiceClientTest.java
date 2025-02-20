/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.configuration.CloudClientConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.ApiClientTestDataFactory;

/**
 * Unit test for {@link ApiClientServiceClient}
 */
@ActiveProfiles("test")
@RestClientTest(ApiClientServiceClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
@Import(CloudClientConfiguration.class)
public class ApiClientServiceClientTest {

    @Autowired
    private ApiClientServiceClient apiClientServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    public void shouldGetApiClient() throws ExceptionClient, JsonProcessingException {
        // Given
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient();
        mockServer.expect(once(), requestTo("http://ig:80/repo/apiclients/" + apiClient.getOauth2ClientId()))
                  .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(objectMapper.writeValueAsString(apiClient)));

        // When
        ApiClient apiClientResponse = apiClientServiceClient.getApiClient(apiClient.getOauth2ClientId());

        // Then
        assertThat(apiClientResponse).isEqualTo(apiClient);
    }

    @Test
    public void shouldGetNotFoundApiClient() {
        // Given
        ApiClient apiClient = ApiClientTestDataFactory.aValidApiClient();
        mockServer.expect(once(), requestTo("http://ig:80/repo/apiclients/" + apiClient.getOauth2ClientId()))
                  .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // When
        ExceptionClient exception = catchThrowableOfType(() -> apiClientServiceClient.getApiClient(apiClient.getOauth2ClientId()), ExceptionClient.class);

        // Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(exception.getErrorClient().getErrorType().getErrorCode()).isEqualTo(ErrorType.NOT_FOUND.getErrorCode());
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.NOT_FOUND.getInternalCode());
    }
}
