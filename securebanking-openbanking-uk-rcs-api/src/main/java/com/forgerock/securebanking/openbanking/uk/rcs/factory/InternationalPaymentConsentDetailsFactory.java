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
package com.forgerock.securebanking.openbanking.uk.rcs.factory;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAmount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRExchangeRateInformation;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.InternationalPaymentConsentDetails;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.json.utils.JsonUtilValidation.isNotNull;
import static com.forgerock.securebanking.platform.client.services.ConsentServiceInterface.log;
import static java.util.Objects.requireNonNull;

/**
 * International Payment consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
public class InternationalPaymentConsentDetailsFactory implements ConsentDetailsFactory<InternationalPaymentConsentDetails> {
    private final InternationalPaymentConsentDetails details;

    @Autowired
    public InternationalPaymentConsentDetailsFactory(InternationalPaymentConsentDetails details) {
        this.details = details;
    }

    @Override
    public InternationalPaymentConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (isNotNull(data.get(EXCHANGE_RATE_INFORMATION))) {
                    setExchangeRateInformation(data.getAsJsonObject(EXCHANGE_RATE_INFORMATION));
                }

                if (isNotNull(data.get(INITIATION))) {
                    JsonObject initiation = data.getAsJsonObject(INITIATION);

                    details.setPaymentReference(
                            isNotNull(initiation.get(REMITTANCE_INFORMATION)) && isNotNull(initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE))
                                    ? initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE).getAsString()
                                    : null
                    );

                    details.setCurrencyOfTransfer(
                            isNotNull(initiation.get(CURRENCY_OF_TRANSFER))
                                    ? initiation.get(CURRENCY_OF_TRANSFER).getAsString()
                                    : null
                    );

                    if (isNotNull(initiation.get(INSTRUCTED_AMOUNT))) {
                        setInstructedAmount(initiation.getAsJsonObject(INSTRUCTED_AMOUNT));
                    }

                    if (isNotNull(data.get(CHARGES))) {
                        setCharges(data.getAsJsonArray(CHARGES));
                    }
                }
            }
        }
        return details;
    }

    @Override
    public IntentType getIntentType() {
        return details.getIntentType();
    }

    private void setInstructedAmount(JsonObject instructedAmount) {
        details.setInstructedAmount(FRAmount.builder().build());
        details.getInstructedAmount().setAmount(
                isNotNull(instructedAmount.get(AMOUNT)) ? instructedAmount.get(AMOUNT).getAsString() : null
        );
        details.getInstructedAmount().setCurrency(
                isNotNull(instructedAmount.get(CURRENCY)) ? instructedAmount.get(CURRENCY).getAsString() : null
        );
    }

    private void setCharges(JsonArray charges) {
        details.setCharges(FRAmount.builder().build());
        Double amount = 0.0;

        for (JsonElement charge : charges) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            if (chargeAmount.get(CURRENCY).getAsString().equals(details.getInstructedAmount().getCurrency())) {
                amount += chargeAmount.get(AMOUNT).getAsDouble();
            } else {
                if (details.getExchangeRateInformation().getExchangeRate() != null) {
                    amount += chargeAmount.get(AMOUNT).getAsDouble() *
                            details.getExchangeRateInformation().getExchangeRate().doubleValue();
                } else {
                    throw new IllegalArgumentException("Exchange Rate value is missing");
                }
            }
        }

        details.getCharges().setCurrency(details.getInstructedAmount().getCurrency());
        details.getCharges().setAmount(amount.toString());
    }

    private void setExchangeRateInformation(JsonObject exchangeRateInformation) {
        details.setExchangeRateInformation(FRExchangeRateInformation.builder().build());
        details.getExchangeRateInformation().setUnitCurrency(
                isNotNull(exchangeRateInformation.get(UNIT_CURRENCY))
                        ? exchangeRateInformation.get(UNIT_CURRENCY).getAsString() : null
        );
        String exchangeRate = isNotNull(exchangeRateInformation.get(EXCHANGE_RATE))
                ? exchangeRateInformation.get(EXCHANGE_RATE).getAsString() : null;
        if (isNotNull(exchangeRate)) {
            try {
                BigDecimal exchangeRateBigDecimal = new BigDecimal(exchangeRate);
                details.getExchangeRateInformation().setExchangeRate(exchangeRateBigDecimal);
            } catch (NumberFormatException e) {
                log.error("(InternationalPaymentConsentDetails) the exchange rate couldn't be set");
            }
        }
        details.getExchangeRateInformation().setRateType(
                isNotNull(exchangeRateInformation.get(RATE_TYPE))
                        ? FRExchangeRateInformation.FRRateType.fromValue(exchangeRateInformation.get(RATE_TYPE).getAsString())
                        : null
        );
        details.getExchangeRateInformation().setContractIdentification(
                isNotNull(exchangeRateInformation.get(CONTRACT_IDENTIFICATION))
                        ? exchangeRateInformation.get(CONTRACT_IDENTIFICATION).getAsString()
                        : null);
        details.getExchangeRateInformation().setExpirationDateTime(
                isNotNull(exchangeRateInformation.get(EXPIRATION_DATETIME))
                        ? Instant.parse(exchangeRateInformation.get(EXPIRATION_DATETIME).getAsString()).toDateTime()
                        : null
        );
    }
}
