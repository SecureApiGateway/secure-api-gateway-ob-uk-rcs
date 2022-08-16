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
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.InternationalPaymentConsentDetails;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.DomesticScheduledPaymentConsentDetailsConverter.DATE_TIME_FORMATTER;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.INTERNATIONAL_PAYMENT_INTENT_ID;
import static com.forgerock.securebanking.platform.client.test.support.InternationalPaymentConsentDetailsTestFactory.aValidInternationalPaymentConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link InternationalPaymentConsentDetailsConverter}
 */
@Slf4j
public class InternationalPaymentConsentDetailsConverterTest {
    @Test
    public void shouldConvertConsentDetailsToInternationalPaymentConsentDetails() {
        // Given
        JsonObject consentDetails = aValidInternationalPaymentConsentDetails(INTERNATIONAL_PAYMENT_INTENT_ID);

        // When
        InternationalPaymentConsentDetails internationalPaymentConsentDetails = InternationalPaymentConsentDetailsConverter.getInstance().toInternationalPaymentConsentDetails(consentDetails);

        // Then
        JsonObject data = consentDetails.getAsJsonObject("data");
        JsonObject initiation = data.getAsJsonObject("Initiation");

        assertThat(internationalPaymentConsentDetails.getInstructedAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject("InstructedAmount").get("Amount").getAsString());

        assertThat(internationalPaymentConsentDetails.getInstructedAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject("InstructedAmount").get("Currency").getAsString());

        assertThat(internationalPaymentConsentDetails.getExchangeRateInformation().getExchangeRate())
                .isEqualTo(new BigDecimal(data.getAsJsonObject("ExchangeRateInformation").get("ExchangeRate").getAsString()));
        assertThat(internationalPaymentConsentDetails.getExchangeRateInformation().getUnitCurrency())
                .isEqualTo(data.getAsJsonObject("ExchangeRateInformation").get("UnitCurrency").getAsString());
        assertThat(internationalPaymentConsentDetails.getExchangeRateInformation().getRateType())
                .isEqualTo(FRExchangeRateInformation.FRRateType.fromValue(data.getAsJsonObject("ExchangeRateInformation").get("RateType").getAsString()));
        assertThat(internationalPaymentConsentDetails.getExchangeRateInformation().getContractIdentification())
                .isEqualTo(data.getAsJsonObject("ExchangeRateInformation").get("ContractIdentification").getAsString());
        assertThat(internationalPaymentConsentDetails.getExchangeRateInformation().getExpirationDateTime())
                .isEqualTo(DATE_TIME_FORMATTER.parseDateTime(data.getAsJsonObject("ExchangeRateInformation").get("ExpirationDateTime").getAsString()));

        assertThat(internationalPaymentConsentDetails.getMerchantName()).isEqualTo(consentDetails.get("oauth2ClientName").getAsString());

        assertThat(internationalPaymentConsentDetails.getPaymentReference())
                .isEqualTo(initiation.getAsJsonObject("RemittanceInformation").get("Reference").getAsString());

        assertThat(internationalPaymentConsentDetails.getCurrencyOfTransfer())
                .isEqualTo(initiation.get("CurrencyOfTransfer").getAsString());

        assertThat(internationalPaymentConsentDetails.getCharges())
                .isNotNull();
    }
}
