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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRExchangeRateInformation;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.InternationalScheduledPaymentConsentDetails;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Instant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID;
import static com.forgerock.securebanking.platform.client.test.support.InternationalScheduledPaymentConsentDetailsTestFactory.aValidInternationalScheduledPaymentConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link InternationalScheduledPaymentConsentDetailsConverter}
 */
@Slf4j
public class InternationalScheduledPaymentConsentDetailsConverterTest {
    @Test
    public void shouldConvertConsentDetailsToInternationalScheduledPaymentConsentDetails() {
        // Given
        JsonObject consentDetails = aValidInternationalScheduledPaymentConsentDetails(INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID);

        // When
        InternationalScheduledPaymentConsentDetails internationalScheduledPaymentConsentDetails = InternationalScheduledPaymentConsentDetailsConverter.getInstance().toInternationalScheduledPaymentConsentDetails(consentDetails);

        // Then
        JsonObject data = consentDetails.getAsJsonObject(OB_INTENT_OBJECT).getAsJsonObject(DATA);
        JsonObject initiation = data.getAsJsonObject(INITIATION);

        assertThat(internationalScheduledPaymentConsentDetails.getInstructedAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(INSTRUCTED_AMOUNT).get(AMOUNT).getAsString());

        assertThat(internationalScheduledPaymentConsentDetails.getInstructedAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(INSTRUCTED_AMOUNT).get(CURRENCY).getAsString());

        JsonObject exchangeRateInformation = data.getAsJsonObject(EXCHANGE_RATE_INFORMATION);
        assertThat(internationalScheduledPaymentConsentDetails.getExchangeRateInformation().getExchangeRate())
                .isEqualTo(new BigDecimal(exchangeRateInformation.get(EXCHANGE_RATE).getAsString()));

        assertThat(internationalScheduledPaymentConsentDetails.getExchangeRateInformation().getUnitCurrency())
                .isEqualTo(exchangeRateInformation.get(UNIT_CURRENCY).getAsString());

        assertThat(internationalScheduledPaymentConsentDetails.getExchangeRateInformation().getRateType())
                .isEqualTo(FRExchangeRateInformation.FRRateType.fromValue(exchangeRateInformation.get(RATE_TYPE).getAsString()));

        assertThat(internationalScheduledPaymentConsentDetails.getExchangeRateInformation().getContractIdentification())
                .isEqualTo(exchangeRateInformation.get(CONTRACT_IDENTIFICATION).getAsString());

        assertThat(internationalScheduledPaymentConsentDetails.getExchangeRateInformation().getExpirationDateTime())
                .isEqualTo(Instant.parse(exchangeRateInformation.get(EXPIRATION_DATETIME).getAsString()).toDateTime());

        assertThat(internationalScheduledPaymentConsentDetails.getPaymentReference())
                .isEqualTo(initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE).getAsString());

        assertThat(internationalScheduledPaymentConsentDetails.getCurrencyOfTransfer())
                .isEqualTo(initiation.get(CURRENCY_OF_TRANSFER).getAsString());

        assertThat(internationalScheduledPaymentConsentDetails.getPaymentDate())
                .isEqualTo(Instant.parse(initiation.get(REQUESTED_EXECUTION_DATETIME).getAsString()).toDateTime());

        assertThat(internationalScheduledPaymentConsentDetails.getCharges())
                .isNotNull();
    }
}
