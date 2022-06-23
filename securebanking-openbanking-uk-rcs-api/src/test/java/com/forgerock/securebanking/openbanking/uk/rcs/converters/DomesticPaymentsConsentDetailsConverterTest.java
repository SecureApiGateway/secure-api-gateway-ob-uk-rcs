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

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticPaymentsConsentDetails;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.DOMESTIC_PAYMENT_INTENT_ID;
import static com.forgerock.securebanking.platform.client.test.support.DomesticPaymentAccessConsentDetailsTestFactory.aValidDomesticPaymentConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DomesticPaymentConsentDetailsConverter}
 */
@Slf4j
public class DomesticPaymentsConsentDetailsConverterTest {

    @Test
    public void shouldConvertConsentDetailsToDomesticPaymentsConsentDetails() {
        // Given
        JsonObject consentDetails = aValidDomesticPaymentConsentDetails(DOMESTIC_PAYMENT_INTENT_ID);

        // When
        DomesticPaymentsConsentDetails domesticPaymentsConsentDetails = DomesticPaymentConsentDetailsConverter.getInstance().toDomesticPaymentConsentDetails(consentDetails);

        // Then
        JsonObject initiation = consentDetails.getAsJsonObject("data").getAsJsonObject("Initiation");

        assertThat(domesticPaymentsConsentDetails.getInstructedAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject("InstructedAmount").get("Amount").getAsString());

        assertThat(domesticPaymentsConsentDetails.getInstructedAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject("InstructedAmount").get("Currency").getAsString());

        assertThat(domesticPaymentsConsentDetails.getMerchantName()).isEqualTo( consentDetails.get("oauth2ClientName").getAsString());

        assertThat(domesticPaymentsConsentDetails.getPaymentReference())
                .isEqualTo(initiation.getAsJsonObject("RemittanceInformation").get("Reference").getAsString());
    }
}
