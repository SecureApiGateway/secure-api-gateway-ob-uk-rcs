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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticStandingOrderDataInitiation;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.forgerock.FRFrequency;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticStandingOrderConsentDetails;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.json.utils.JsonUtilValidation.isNotNull;
import static java.util.Objects.requireNonNull;

/**
 * Domestic Standing Order consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
public class DomesticStandingOrderConsentDetailsFactory implements ConsentDetailsFactory<DomesticStandingOrderConsentDetails> {
    private final DomesticStandingOrderConsentDetails details;

    @Autowired
    public DomesticStandingOrderConsentDetailsFactory(DomesticStandingOrderConsentDetails details) {
        this.details = details;
    }

    @Override
    public DomesticStandingOrderConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
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

                    setStandingOrder(
                            isNotNull(initiation.get(FINAL_PAYMENT_DATETIME))
                                    ? initiation.get(FINAL_PAYMENT_DATETIME) : null,
                            isNotNull(initiation.get(FINAL_PAYMENT_AMOUNT))
                                    ? initiation.getAsJsonObject(FINAL_PAYMENT_AMOUNT) : null,
                            isNotNull(initiation.get(FIRST_PAYMENT_DATETIME))
                                    ? initiation.get(FIRST_PAYMENT_DATETIME) : null,
                            isNotNull(initiation.get(FIRST_PAYMENT_AMOUNT))
                                    ? initiation.getAsJsonObject(FIRST_PAYMENT_AMOUNT) : null,
                            isNotNull(initiation.get(RECURRING_PAYMENT_DATETIME))
                                    ? initiation.get(RECURRING_PAYMENT_DATETIME) : null,
                            isNotNull(initiation.get(RECURRING_PAYMENT_AMOUNT))
                                    ? initiation.getAsJsonObject(RECURRING_PAYMENT_AMOUNT) : null,
                            initiation.get(FREQUENCY)
                    );

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

    private void setCharges(JsonArray chargesArray) {
        details.setCharges(FRAmount.builder().build());
        Double amount = 0.0;
        for (JsonElement charge : chargesArray) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            amount += chargeAmount.get(AMOUNT).getAsDouble();
        }
        details.getCharges().setAmount(amount.toString());
        details.getCharges().setCurrency(details.getStandingOrder().getFirstPaymentAmount().getCurrency());
    }

    private void setStandingOrder(
            JsonElement finalPaymentDateTime,
            JsonObject finalPaymentAmount,
            JsonElement firstPaymentDateTime,
            JsonObject firstPaymentAmount,
            JsonElement recurringPaymentDateTime,
            JsonObject recurringPaymentAmount,
            JsonElement frequency
    ) {
        FRWriteDomesticStandingOrderDataInitiation standingOrderData = new FRWriteDomesticStandingOrderDataInitiation();

        if (isNotNull(finalPaymentDateTime)) {
            standingOrderData.setFinalPaymentDateTime(Instant.parse(finalPaymentDateTime.getAsString()).toDateTime());
        }
        if (isNotNull(finalPaymentAmount)) {
            FRAmount frFinalPaymentAmount = new FRAmount();
            frFinalPaymentAmount.setAmount(
                    isNotNull(finalPaymentAmount.get(AMOUNT)) ? finalPaymentAmount.get(AMOUNT).getAsString() : null
            );
            frFinalPaymentAmount.setCurrency(
                    isNotNull(finalPaymentAmount.get(CURRENCY)) ? finalPaymentAmount.get(CURRENCY).getAsString() : null
            );
            standingOrderData.setFinalPaymentAmount(frFinalPaymentAmount);
        }

        if (isNotNull(firstPaymentDateTime)) {
            standingOrderData.setFirstPaymentDateTime(Instant.parse(firstPaymentDateTime.getAsString()).toDateTime());
        }
        if (isNotNull(firstPaymentAmount)) {
            FRAmount frFirstPaymentAmount = new FRAmount();
            frFirstPaymentAmount.setAmount(
                    isNotNull(firstPaymentAmount.get(AMOUNT)) ? firstPaymentAmount.get(AMOUNT).getAsString() : null
            );
            frFirstPaymentAmount.setCurrency(
                    isNotNull(firstPaymentAmount.get(CURRENCY)) ? firstPaymentAmount.get(CURRENCY).getAsString() : null
            );
            standingOrderData.setFirstPaymentAmount(frFirstPaymentAmount);
        }

        if (isNotNull(recurringPaymentDateTime)) {
            standingOrderData.setRecurringPaymentDateTime(
                    Instant.parse(recurringPaymentDateTime.getAsString()).toDateTime()
            );
        }
        if (isNotNull(recurringPaymentAmount)) {
            FRAmount frRecurringPaymentAmount = new FRAmount();
            frRecurringPaymentAmount.setAmount(
                    isNotNull(recurringPaymentAmount.get(AMOUNT))
                            ? recurringPaymentAmount.get(AMOUNT).getAsString() : null
            );
            frRecurringPaymentAmount.setCurrency(
                    isNotNull(recurringPaymentAmount.get(CURRENCY))
                            ? recurringPaymentAmount.get(CURRENCY).getAsString() : null
            );
            standingOrderData.setRecurringPaymentAmount(frRecurringPaymentAmount);
        }

        if (isNotNull(frequency)) {
            String frequencyType = frequency.getAsString();
            FRFrequency frFrequency = new FRFrequency(frequencyType);
            String sentence = frFrequency.getSentence();
            standingOrderData.setFrequency(sentence);
        }

        details.setStandingOrder(standingOrderData);
    }
}
