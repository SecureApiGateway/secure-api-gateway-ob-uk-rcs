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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrderDataInitiation;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.forgerock.FRFrequency;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.InternationalStandingOrderConsentDetails;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.json.utils.JsonUtilValidation.isNotNull;
import static java.util.Objects.requireNonNull;

/**
 * International Standing Order consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
public class InternationalStandingOrderConsentDetailsFactory implements ConsentDetailsFactory<InternationalStandingOrderConsentDetails> {

    @Override
    public InternationalStandingOrderConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        InternationalStandingOrderConsentDetails details = new InternationalStandingOrderConsentDetails();
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (isNotNull(data.get(INITIATION))) {

                    JsonObject initiation = data.getAsJsonObject(INITIATION);

                    details.setPaymentReference(
                            isNotNull(initiation.get(REFERENCE)) ?
                                    initiation.get(REFERENCE).getAsString() :
                                    null
                    );

                    details.setCurrencyOfTransfer(
                            isNotNull(initiation.get(CURRENCY_OF_TRANSFER)) ?
                                    initiation.get(CURRENCY_OF_TRANSFER).getAsString() :
                                    null
                    );

                    details.setInternationalStandingOrderDataInitiation(
                            decodeInternationalStandingOrderDataInitiation(
                                    isNotNull(initiation.get(FIRST_PAYMENT_DATETIME)) ? initiation.get(FIRST_PAYMENT_DATETIME) : null,
                                    isNotNull(initiation.get(FINAL_PAYMENT_DATETIME)) ? initiation.get(FINAL_PAYMENT_DATETIME) : null,
                                    isNotNull(initiation.get(INSTRUCTED_AMOUNT)) ? initiation.getAsJsonObject(INSTRUCTED_AMOUNT) : null,
                                    initiation.get(FREQUENCY)
                            )
                    );

                    if (isNotNull(data.get(CHARGES))) {
                        details.setCharges(
                                decodeCharges(
                                        data.getAsJsonArray(CHARGES),
                                        details.getInternationalStandingOrderDataInitiation().getInstructedAmount().getCurrency()
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
        return IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT;
    }

    private FRAmount decodeCharges(JsonArray chargesArray, String currency) {
        FRAmount charges = new FRAmount();
        Double amount = 0.0;

        for (JsonElement charge : chargesArray) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            amount += chargeAmount.get(AMOUNT).getAsDouble();
        }

        charges.setCurrency(currency);
        charges.setAmount(amount.toString());
        return charges;
    }

    private FRWriteInternationalStandingOrderDataInitiation decodeInternationalStandingOrderDataInitiation(
            JsonElement firstPaymentDateTime,
            JsonElement finalPaymentDateTime,
            JsonObject instructedAmount,
            JsonElement frequency
    ) {
        FRWriteInternationalStandingOrderDataInitiation standingOrderData = new FRWriteInternationalStandingOrderDataInitiation();

        if (isNotNull(firstPaymentDateTime)) {
            standingOrderData.setFirstPaymentDateTime(Instant.parse(firstPaymentDateTime.getAsString()).toDateTime());
        }

        if (isNotNull(finalPaymentDateTime)) {
            standingOrderData.setFinalPaymentDateTime(Instant.parse(finalPaymentDateTime.getAsString()).toDateTime());
        }

        if (isNotNull(instructedAmount)) {
            FRAmount frInstructedAmount = new FRAmount();
            frInstructedAmount.setAmount(
                    isNotNull(instructedAmount.get(AMOUNT)) ? instructedAmount.get(AMOUNT).getAsString() : null
            );
            frInstructedAmount.setCurrency(
                    isNotNull(instructedAmount.get(CURRENCY)) ? instructedAmount.get(CURRENCY).getAsString() : null
            );
            standingOrderData.setInstructedAmount(frInstructedAmount);
        }

        if (isNotNull(frequency)) {
            String frequencyType = frequency.getAsString();
            FRFrequency frFrequency = new FRFrequency(frequencyType);
            String sentence = frFrequency.getSentence();
            standingOrderData.setFrequency(sentence);
        }

        return standingOrderData;
    }
}
