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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAmount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrderDataInitiation;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.forgerock.FRFrequency;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Models the consent data for an international standing order.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class InternationalStandingOrderConsentDetails extends ConsentDetails {

    private FRWriteInternationalStandingOrderDataInitiation internationalStandingOrder;
    private FRAmount charges;
    private DateTime expiredDate;
    private String currencyOfTransfer;
    private String paymentReference;

    @Override
    public InternationalStandingOrderConsentDetails getInstance() {
        return new InternationalStandingOrderConsentDetails();
    }

    @Override
    public void mapping(JsonObject consentDetails) {

        if (!consentDetails.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = consentDetails.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (isNotNull(data.get(INITIATION))) {

                    JsonObject initiation = data.getAsJsonObject(INITIATION);

                    paymentReference = isNotNull(initiation.get(REFERENCE)) ?
                            initiation.get(REFERENCE).getAsString() : null;

                    currencyOfTransfer = isNotNull(initiation.get(CURRENCY_OF_TRANSFER)) ?
                            initiation.get(CURRENCY_OF_TRANSFER).getAsString() : null;

                    this.setInternationalStandingOrder(
                            isNotNull(initiation.get(FIRST_PAYMENT_DATETIME)) ? initiation.get(FIRST_PAYMENT_DATETIME) : null,
                            isNotNull(initiation.get(FINAL_PAYMENT_DATETIME)) ? initiation.get(FINAL_PAYMENT_DATETIME) : null,
                            isNotNull(initiation.get(INSTRUCTED_AMOUNT)) ? initiation.getAsJsonObject(INSTRUCTED_AMOUNT) : null,
                            initiation.get(FREQUENCY)
                    );

                    if (isNotNull(data.get(CHARGES))) {
                        setCharges(data.getAsJsonArray(CHARGES));
                    }
                }
            }
        }
    }

    public void setInternationalStandingOrder(FRWriteInternationalStandingOrderDataInitiation internationalStandingOrder) {
        this.internationalStandingOrder = internationalStandingOrder;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT;
    }

    public void setCharges(JsonArray charges) {
        this.charges = new FRAmount();
        Double amount = 0.0;

        for (JsonElement charge : charges) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            amount += chargeAmount.get(AMOUNT).getAsDouble();
        }

        this.charges.setCurrency(internationalStandingOrder.getInstructedAmount().getCurrency());
        this.charges.setAmount(amount.toString());
    }

    public void setInternationalStandingOrder(
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

        this.internationalStandingOrder = standingOrderData;
    }

}
