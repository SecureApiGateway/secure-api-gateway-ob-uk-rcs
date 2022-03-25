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
package com.forgerock.securebanking.platform.client.services;

import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentDetails;
import com.forgerock.securebanking.platform.client.models.base.ConsentRequest;
import com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory;
import com.forgerock.securebanking.platform.client.test.support.ConsentDetailsRequestTestDataFactory;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

/**
 * Unit test for {@link ConsentService }
 */
public class ConsentServiceTest extends BaseServiceClientTest {

    @InjectMocks
    private ConsentService consentService;

    public void shouldGetConsentDetails() throws ExceptionClient {
        // Given
        ConsentRequest consentRequest = ConsentDetailsRequestTestDataFactory.aValidAccountConsentDetailsRequest();
        JsonObject details = AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails(consentRequest.getIntentId());
        details.addProperty("oauth2ClientId", consentRequest.getClientId());
        when(restTemplate.exchange(
                        anyString(),
                        eq(GET),
                        isNull(),
                        eq(JsonObject.class)
                )
        ).thenReturn(ResponseEntity.ok(details));

        // When
        JsonObject consentDetails = consentService.getConsent(consentRequest);

        // Then
        assertThat(consentDetails).isNotNull();
        assertThat(consentDetails).isEqualTo(details);
    }

    public void shouldGetInvalidRequestConsentDetails() {
        // Given
        ConsentRequest ConsentRequest = ConsentDetailsRequestTestDataFactory.aValidAccountConsentDetailsRequest();
        JsonObject consentDetails = AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails(ConsentRequest.getIntentId());
        when(restTemplate.exchange(
                        anyString(),
                        eq(GET),
                        isNull(),
                        eq(JsonObject.class)
                )
        ).thenReturn(ResponseEntity.ok(consentDetails));

        // When
        ExceptionClient exception = catchThrowableOfType(() -> consentService.getConsent(ConsentRequest), ExceptionClient.class);

        // Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST);
        assertThat(exception.getErrorClient().getErrorType().getErrorCode()).isEqualTo(ErrorType.INVALID_REQUEST.getErrorCode());
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.INVALID_REQUEST.getInternalCode());
    }

    public void shouldGetNotFoundConsentDetails() {
        // Given
        ConsentRequest ConsentRequest = ConsentDetailsRequestTestDataFactory.aValidAccountConsentDetailsRequest();
        when(restTemplate.exchange(
                        anyString(),
                        eq(GET),
                        isNull(),
                        eq(AccountConsentDetails.class)
                )
        ).thenReturn(null);

        // When
        ExceptionClient exception = catchThrowableOfType(() -> consentService.getConsent(ConsentRequest), ExceptionClient.class);

        // Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(exception.getErrorClient().getErrorType().getErrorCode()).isEqualTo(ErrorType.NOT_FOUND.getErrorCode());
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.NOT_FOUND.getInternalCode());
    }
}
