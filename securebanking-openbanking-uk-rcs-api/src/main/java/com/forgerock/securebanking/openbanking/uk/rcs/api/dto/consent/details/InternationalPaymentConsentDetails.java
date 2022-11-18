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
package com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAmount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRExchangeRateInformation;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joda.time.Instant;

import java.math.BigDecimal;
import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;
import static com.forgerock.securebanking.platform.client.services.ConsentServiceInterface.log;

/**
 * Models the consent data for an international payment.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class InternationalPaymentConsentDetails extends ConsentDetails {

    private FRAmount instructedAmount;
    private FRExchangeRateInformation exchangeRateInformation;
    private FRAmount charges;
    private String currencyOfTransfer;
    private String paymentReference;
    private List<FRAccountWithBalance> accounts;

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_INTERNATIONAL_CONSENT;
    }

    public void setInstructedAmount(JsonObject instructedAmount) {
        if (!isNotNull(instructedAmount))
            this.instructedAmount = null;
        else {
            this.instructedAmount = new FRAmount();
            this.instructedAmount.setAmount(
                    isNotNull(instructedAmount.get(AMOUNT)) ? instructedAmount.get(AMOUNT).getAsString() : null
            );
            this.instructedAmount.setCurrency(
                    isNotNull(instructedAmount.get(CURRENCY)) ? instructedAmount.get(CURRENCY).getAsString() : null
            );
        }
    }

    public void setCharges(JsonArray charges) {
        if (!isNotNull(charges)) {
            this.charges = null;
        } else {
            this.charges = new FRAmount();
            Double amount = 0.0;

            for (JsonElement charge : charges) {
                JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
                if (chargeAmount.get(CURRENCY).getAsString().equals(instructedAmount.getCurrency())) {
                    amount += chargeAmount.get(AMOUNT).getAsDouble();
                } else {
                    if (exchangeRateInformation.getExchangeRate() != null) {
                        amount += chargeAmount.get(AMOUNT).getAsDouble() * exchangeRateInformation.getExchangeRate().doubleValue();
                    } else {
                        throw new IllegalArgumentException("Exchange Rate value is missing");
                    }
                }
            }

            this.charges.setCurrency(instructedAmount.getCurrency());
            this.charges.setAmount(amount.toString());
        }
    }

    public void setExchangeRateInformation(JsonObject exchangeRateInformation) {
        if (!isNotNull(exchangeRateInformation))
            this.exchangeRateInformation = null;
        else {
            this.exchangeRateInformation = new FRExchangeRateInformation();
            this.exchangeRateInformation.setUnitCurrency(
                    isNotNull(exchangeRateInformation.get(UNIT_CURRENCY))
                            ? exchangeRateInformation.get(UNIT_CURRENCY).getAsString() : null
            );
            String exchangeRate = isNotNull(exchangeRateInformation.get(EXCHANGE_RATE))
                    ? exchangeRateInformation.get(EXCHANGE_RATE).getAsString() : null;
            if (isNotNull(exchangeRate)) {
                try {
                    BigDecimal exchangeRateBigDecimal = new BigDecimal(exchangeRate);
                    this.exchangeRateInformation.setExchangeRate(exchangeRateBigDecimal);
                } catch (NumberFormatException e) {
                    log.error("(InternationalPaymentConsentDetails) the exchange rate couldn't be set");
                }
            }
            this.exchangeRateInformation.setRateType(
                    isNotNull(exchangeRateInformation.get(RATE_TYPE))
                            ? FRExchangeRateInformation.FRRateType.fromValue(exchangeRateInformation.get(RATE_TYPE).getAsString())
                            : null
            );
            this.exchangeRateInformation.setContractIdentification(
                    isNotNull(exchangeRateInformation.get(CONTRACT_IDENTIFICATION))
                            ? exchangeRateInformation.get(CONTRACT_IDENTIFICATION).getAsString()
                            : null);
            this.exchangeRateInformation.setExpirationDateTime(
                    isNotNull(exchangeRateInformation.get(EXPIRATION_DATETIME))
                            ? Instant.parse(exchangeRateInformation.get(EXPIRATION_DATETIME).getAsString()).toDateTime()
                            : null
            );
        }
    }
}
