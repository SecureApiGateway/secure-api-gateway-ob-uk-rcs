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

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.User;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.UserTestDataFactory;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBHeaders;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

/**
 * Unit test for {@link UserServiceClient}
 */
public class UserServiceClientTest extends BaseServiceClientTest {

    @InjectMocks
    private UserServiceClient userServiceClient;

    @Test
    public void shouldGetUser() throws ExceptionClient {
        // Given
        User user = UserTestDataFactory.aValidUser();

        final ArgumentCaptor<HttpEntity> entityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(
                anyString(),
                eq(GET),
                entityArgumentCaptor.capture(),
                eq(User.class))
        ).thenReturn(ResponseEntity.ok(user));

        // When
        User userResponse = userServiceClient.getUser(user.getId());

        // Then
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(user.getId());
        assertThat(userResponse.getUserName()).isEqualTo(user.getUserName());
        assertThat(userResponse.getAccountStatus()).isEqualTo(user.getAccountStatus());
        assertThat(userResponse.getMail()).isEqualTo(user.getMail());
        assertThat(userResponse.getSurname()).isEqualTo(user.getSurname());
        assertThat(userResponse.getGivenName()).isEqualTo(user.getGivenName());
        assertThat(entityArgumentCaptor.getValue().getHeaders().get(OBHeaders.X_FAPI_INTERACTION_ID)).isNotNull();
    }

    @Test
    public void shouldGetNotFoundApiClient() {
        // Given
        User user = UserTestDataFactory.aValidUser();

        // When
        ExceptionClient exception = catchThrowableOfType(() -> userServiceClient.getUser(user.getId()), ExceptionClient.class);

        // Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(exception.getErrorClient().getErrorType().getErrorCode()).isEqualTo(ErrorType.NOT_FOUND.getErrorCode());
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.NOT_FOUND.getInternalCode());
    }
}
