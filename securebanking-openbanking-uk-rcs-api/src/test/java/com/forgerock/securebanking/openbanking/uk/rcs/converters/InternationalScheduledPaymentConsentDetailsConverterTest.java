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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.DomesticScheduledPaymentConsentDetailsConverter.DATE_TIME_FORMATTER;
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
        JsonObject data = consentDetails.getAsJsonObject("data");
        JsonObject initiation = data.getAsJsonObject("Initiation");

        assertThat(internationalScheduledPaymentConsentDetails.getInstructedAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject("InstructedAmount").get("Amount").getAsString());

        assertThat(internationalScheduledPaymentConsentDetails.getInstructedAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject("InstructedAmount").get("Currency").getAsString());

        assertThat(internationalScheduledPaymentConsentDetails.getExchangeRateInformation().getExchangeRate())
                .isEqualTo(new BigDecimal(data.getAsJsonObject("ExchangeRateInformation").get("ExchangeRate").getAsString()));
        assertThat(internationalScheduledPaymentConsentDetails.getExchangeRateInformation().getUnitCurrency())
                .isEqualTo(data.getAsJsonObject("ExchangeRateInformation").get("UnitCurrency").getAsString());
        assertThat(internationalScheduledPaymentConsentDetails.getExchangeRateInformation().getRateType())
                .isEqualTo(FRExchangeRateInformation.FRRateType.fromValue(data.getAsJsonObject("ExchangeRateInformation").get("RateType").getAsString()));
        assertThat(internationalScheduledPaymentConsentDetails.getExchangeRateInformation().getContractIdentification())
                .isEqualTo(data.getAsJsonObject("ExchangeRateInformation").get("ContractIdentification").getAsString());
        assertThat(internationalScheduledPaymentConsentDetails.getExchangeRateInformation().getExpirationDateTime())
                .isEqualTo(DATE_TIME_FORMATTER.parseDateTime(data.getAsJsonObject("ExchangeRateInformation").get("ExpirationDateTime").getAsString()));

        assertThat(internationalScheduledPaymentConsentDetails.getMerchantName()).isEqualTo(consentDetails.get("oauth2ClientName").getAsString());

        assertThat(internationalScheduledPaymentConsentDetails.getPaymentReference())
                .isEqualTo(initiation.getAsJsonObject("RemittanceInformation").get("Reference").getAsString());

        assertThat(internationalScheduledPaymentConsentDetails.getCurrencyOfTransfer())
                .isEqualTo(initiation.get("CurrencyOfTransfer").getAsString());

        assertThat(internationalScheduledPaymentConsentDetails.getPaymentDate())
                .isEqualTo(DATE_TIME_FORMATTER.parseDateTime(initiation.get("RequestedExecutionDateTime").getAsString()));

        assertThat(internationalScheduledPaymentConsentDetails.getCharges())
                .isNotNull();
    }
}
