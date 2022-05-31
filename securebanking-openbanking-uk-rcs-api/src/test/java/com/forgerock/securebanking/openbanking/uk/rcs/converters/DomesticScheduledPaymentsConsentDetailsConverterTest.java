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
package com.forgerock.securebanking.openbanking.uk.rcs.converters;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticScheduledPaymentsConsentDetails;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.DomesticScheduledPaymentConsentDetailsConverter.DATE_TIME_FORMATTER;
import static com.forgerock.securebanking.platform.client.test.support.DomesticScheduledPaymentAccessConsentDetailsTestFactory.aValidDomesticScheduledPaymentConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DomesticScheduledPaymentConsentDetailsConverter}
 */
@Slf4j
public class DomesticScheduledPaymentsConsentDetailsConverterTest {

    @Test
    public void shouldConvertConsentDetailsToDomesticScheduledPaymentsConsentDetails() {
        // Given
        JsonObject consentDetails = aValidDomesticScheduledPaymentConsentDetails(DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID);

        // When
        DomesticScheduledPaymentsConsentDetails domesticScheduledPaymentsConsentDetails = DomesticScheduledPaymentConsentDetailsConverter.getInstance().toDomesticScheduledPaymentConsentDetails(consentDetails);

        // Then
        JsonObject initiation = consentDetails.getAsJsonObject("data").getAsJsonObject("Initiation");

        assertThat(domesticScheduledPaymentsConsentDetails.getInstructedAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject("InstructedAmount").get("Amount").getAsString());

        assertThat(domesticScheduledPaymentsConsentDetails.getInstructedAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject("InstructedAmount").get("Currency").getAsString());

        assertThat(domesticScheduledPaymentsConsentDetails.getMerchantName()).isEqualTo( consentDetails.get("oauth2ClientName").getAsString());

        assertThat(domesticScheduledPaymentsConsentDetails.getPaymentReference())
                .isEqualTo(initiation.getAsJsonObject("RemittanceInformation").get("Reference").getAsString());

        assertThat(domesticScheduledPaymentsConsentDetails.getPaymentDate())
                .isEqualTo(DATE_TIME_FORMATTER.parseDateTime(initiation.get("RequestedExecutionDateTime").getAsString()));
    }
}