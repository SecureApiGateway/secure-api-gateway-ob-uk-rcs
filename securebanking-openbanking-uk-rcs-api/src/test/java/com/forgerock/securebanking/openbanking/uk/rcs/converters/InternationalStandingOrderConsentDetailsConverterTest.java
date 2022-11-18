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

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.InternationalStandingOrderConsentDetails;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.INTERNATIONAL_STANDING_ORDER_INTENT_ID;
import static com.forgerock.securebanking.platform.client.test.support.InternationalStandingOrderConsentDetailsTestFactory.aValidInternationalStandingOrderConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link InternationalScheduledPaymentConsentDetailsConverter}
 */
@Slf4j
public class InternationalStandingOrderConsentDetailsConverterTest {
    @Test
    public void shouldConvertConsentDetailsToInternationalStandingOrderConsentDetails() {
        // Given
        JsonObject consentDetails = aValidInternationalStandingOrderConsentDetails(INTERNATIONAL_STANDING_ORDER_INTENT_ID);

        // When
        InternationalStandingOrderConsentDetails internationalStandingOrderConsentDetails =
                InternationalStandingOrderConsentDetailsConverter.getInstance()
                        .toInternationalStandingOrderConsentDetails(consentDetails);

        // Then
        JsonObject data = consentDetails.getAsJsonObject(OB_INTENT_OBJECT).getAsJsonObject(DATA);
        JsonObject initiation = data.getAsJsonObject(INITIATION);

        assertThat(internationalStandingOrderConsentDetails.getInternationalStandingOrder().getInstructedAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(INSTRUCTED_AMOUNT).get(AMOUNT).getAsString());

        assertThat(internationalStandingOrderConsentDetails.getInternationalStandingOrder().getInstructedAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(INSTRUCTED_AMOUNT).get(CURRENCY).getAsString());

        assertThat(internationalStandingOrderConsentDetails.getCurrencyOfTransfer())
                .isEqualTo(initiation.get(CURRENCY_OF_TRANSFER).getAsString());

        assertThat(internationalStandingOrderConsentDetails.getCharges())
                .isNotNull();
    }
}
