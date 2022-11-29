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
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Models the consent data for a domestic payment.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class DomesticPaymentConsentDetails extends ConsentDetails {
    private FRAmount instructedAmount;
    private String paymentReference;
    private FRAmount charges;

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

                    if (isNotNull(initiation.get(INSTRUCTED_AMOUNT))) {
                        setInstructedAmount(initiation.getAsJsonObject(INSTRUCTED_AMOUNT));
                    }

                    paymentReference = isNotNull(initiation.get(REMITTANCE_INFORMATION)) &&
                            isNotNull(initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE)) ?
                            initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE).getAsString() :
                            null;

                    if (isNotNull(data.get(CHARGES))) {
                        setCharges(data.getAsJsonArray(CHARGES));
                    }
                }
            }
        }
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_DOMESTIC_CONSENT;
    }

    public void setCharges(JsonArray charges) {
        this.charges = new FRAmount();
        Double amount = 0.0;

        for (JsonElement charge : charges) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            amount += chargeAmount.get(AMOUNT).getAsDouble();
        }
        this.charges.setAmount(amount.toString());
        this.charges.setCurrency(instructedAmount.getCurrency());
    }

    public void setInstructedAmount(JsonObject instructedAmount) {
        this.instructedAmount = new FRAmount();
        this.instructedAmount.setAmount(
                isNotNull(instructedAmount.get(AMOUNT)) ? instructedAmount.get(AMOUNT).getAsString() : null
        );
        this.instructedAmount.setCurrency(
                isNotNull(instructedAmount.get(CURRENCY)) ? instructedAmount.get(CURRENCY).getAsString() : null
        );
    }
}
