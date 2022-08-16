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
package com.forgerock.securebanking.openbanking.uk.rcs.converters;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticStandingOrderConsentDetails;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.DomesticStandingOrderConsentDetailsConverter.DATE_TIME_FORMATTER;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.DOMESTIC_STANDING_ORDER_INTENT_ID;
import static com.forgerock.securebanking.platform.client.test.support.DomesticStandingOrderConsentDetailsTestFactory.aValidDomesticStandingOrderConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DomesticStandingOrderConsentDetailsConverter}
 */
@Slf4j
public class DomesticStandingOrderConsentDetailsConverterTest {
    @Test
    public void shouldConvertConsentDetailsToDomesticStandingOrderConsentDetails() {
        // Given
        JsonObject consentDetails = aValidDomesticStandingOrderConsentDetails(DOMESTIC_STANDING_ORDER_INTENT_ID);

        // When
        DomesticStandingOrderConsentDetails domesticStandingOrderConsentDetails = DomesticStandingOrderConsentDetailsConverter.getInstance().toDomesticStandingOrderConsentDetails(consentDetails);

        // Then
        JsonObject initiation = consentDetails.getAsJsonObject("data").getAsJsonObject("Initiation");

        assertThat(domesticStandingOrderConsentDetails.getStandingOrder().getFinalPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject("FinalPaymentAmount").get("Amount").getAsString());
        assertThat(domesticStandingOrderConsentDetails.getStandingOrder().getFinalPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject("FinalPaymentAmount").get("Currency").getAsString());

        assertThat(domesticStandingOrderConsentDetails.getStandingOrder().getFirstPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject("FirstPaymentAmount").get("Amount").getAsString());
        assertThat(domesticStandingOrderConsentDetails.getStandingOrder().getFirstPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject("FirstPaymentAmount").get("Currency").getAsString());

        assertThat(domesticStandingOrderConsentDetails.getStandingOrder().getRecurringPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject("RecurringPaymentAmount").get("Amount").getAsString());
        assertThat(domesticStandingOrderConsentDetails.getStandingOrder().getRecurringPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject("RecurringPaymentAmount").get("Currency").getAsString());

        assertThat(domesticStandingOrderConsentDetails.getStandingOrder().getFinalPaymentDateTime())
                .isEqualTo(DATE_TIME_FORMATTER.parseDateTime(initiation.get("FinalPaymentDateTime").getAsString()));
        assertThat(domesticStandingOrderConsentDetails.getStandingOrder().getFirstPaymentDateTime())
                .isEqualTo(DATE_TIME_FORMATTER.parseDateTime(initiation.get("FirstPaymentDateTime").getAsString()));
        assertThat(domesticStandingOrderConsentDetails.getStandingOrder().getRecurringPaymentDateTime())
                .isEqualTo(DATE_TIME_FORMATTER.parseDateTime(initiation.get("RecurringPaymentDateTime").getAsString()));

        assertThat(domesticStandingOrderConsentDetails.getMerchantName()).isEqualTo(consentDetails.get("oauth2ClientName").getAsString());

        assertThat(domesticStandingOrderConsentDetails.getPaymentReference())
                .isEqualTo(initiation.get("Reference").getAsString());

        assertThat(domesticStandingOrderConsentDetails.getCharges())
                .isNotNull();
    }
}
