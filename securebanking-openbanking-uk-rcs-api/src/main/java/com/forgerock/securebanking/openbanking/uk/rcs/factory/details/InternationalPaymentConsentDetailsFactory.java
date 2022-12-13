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
package com.forgerock.securebanking.openbanking.uk.rcs.factory.details;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAmount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRExchangeRateInformation;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.InternationalPaymentConsentDetails;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joda.time.Instant;
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

    @Override
    public InternationalPaymentConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        InternationalPaymentConsentDetails details = new InternationalPaymentConsentDetails();
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (isNotNull(data.get(EXCHANGE_RATE_INFORMATION))) {
                    details.setExchangeRateInformation(
                            decodeExchangeRateInformation(data.getAsJsonObject(EXCHANGE_RATE_INFORMATION))
                    );
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
                        details.setInstructedAmount(
                                decodeInstructedAmount(initiation.getAsJsonObject(INSTRUCTED_AMOUNT))
                        );
                    }

                    if (isNotNull(data.get(CHARGES))) {
                        details.setCharges(
                                decodeCharges(
                                        data.getAsJsonArray(CHARGES),
                                        details.getInstructedAmount().getCurrency(),
                                        details.getExchangeRateInformation()
                                )
                        );
                    }
                }
            }
        }
        return details;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_INTERNATIONAL_CONSENT;
    }

    private FRAmount decodeInstructedAmount(JsonObject instructedAmount) {
        FRAmount frAmount = new FRAmount();
        frAmount.setAmount(
                isNotNull(instructedAmount.get(AMOUNT)) ? instructedAmount.get(AMOUNT).getAsString() : null
        );
        frAmount.setCurrency(
                isNotNull(instructedAmount.get(CURRENCY)) ? instructedAmount.get(CURRENCY).getAsString() : null
        );
        return frAmount;
    }

    private FRAmount decodeCharges(JsonArray chargesArray, String currency, FRExchangeRateInformation rateInformation) {
        FRAmount charges = new FRAmount();
        Double amount = 0.0;

        for (JsonElement charge : chargesArray) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            if (chargeAmount.get(CURRENCY).getAsString().equals(currency)) {
                amount += chargeAmount.get(AMOUNT).getAsDouble();
            } else {
                if (rateInformation != null) {
                    amount += chargeAmount.get(AMOUNT).getAsDouble() *
                            rateInformation.getExchangeRate().doubleValue();
                } else {
                    throw new IllegalArgumentException("Exchange Rate value is missing");
                }
            }
        }

        charges.setCurrency(currency);
        charges.setAmount(amount.toString());
        return charges;
    }

    private FRExchangeRateInformation decodeExchangeRateInformation(JsonObject exchangeRateInformation) {
        FRExchangeRateInformation rateInformation = new FRExchangeRateInformation();
        rateInformation.setUnitCurrency(
                isNotNull(exchangeRateInformation.get(UNIT_CURRENCY))
                        ? exchangeRateInformation.get(UNIT_CURRENCY).getAsString() : null
        );
        String exchangeRate = isNotNull(exchangeRateInformation.get(EXCHANGE_RATE))
                ? exchangeRateInformation.get(EXCHANGE_RATE).getAsString() : null;
        if (isNotNull(exchangeRate)) {
            try {
                BigDecimal exchangeRateBigDecimal = new BigDecimal(exchangeRate);
                rateInformation.setExchangeRate(exchangeRateBigDecimal);
            } catch (NumberFormatException e) {
                log.error("(InternationalPaymentConsentDetails) the exchange rate couldn't be set");
            }
        }
        rateInformation.setRateType(
                isNotNull(exchangeRateInformation.get(RATE_TYPE))
                        ? FRExchangeRateInformation.FRRateType.fromValue(exchangeRateInformation.get(RATE_TYPE).getAsString())
                        : null
        );
        rateInformation.setContractIdentification(
                isNotNull(exchangeRateInformation.get(CONTRACT_IDENTIFICATION))
                        ? exchangeRateInformation.get(CONTRACT_IDENTIFICATION).getAsString()
                        : null);
        rateInformation.setExpirationDateTime(
                isNotNull(exchangeRateInformation.get(EXPIRATION_DATETIME))
                        ? Instant.parse(exchangeRateInformation.get(EXPIRATION_DATETIME).getAsString()).toDateTime()
                        : null
        );
        return rateInformation;
    }
}