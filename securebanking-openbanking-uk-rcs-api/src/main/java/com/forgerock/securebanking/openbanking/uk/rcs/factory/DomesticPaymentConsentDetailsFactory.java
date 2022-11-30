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
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticPaymentConsentDetails;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.json.utils.JsonUtilValidation.isNotNull;
import static java.util.Objects.requireNonNull;

/**
 * Domestic payment consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
public class DomesticPaymentConsentDetailsFactory implements ConsentDetailsFactory<DomesticPaymentConsentDetails> {

    @Override
    public DomesticPaymentConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        DomesticPaymentConsentDetails details = DomesticPaymentConsentDetails.builder().build();
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (isNotNull(data.get(INITIATION))) {
                    JsonObject initiation = data.getAsJsonObject(INITIATION);

                    if (isNotNull(initiation.get(INSTRUCTED_AMOUNT))) {
                        details.setInstructedAmount(
                                decodeInstructedAmount(initiation.getAsJsonObject(INSTRUCTED_AMOUNT))
                        );
                    }

                    details.setPaymentReference(
                            isNotNull(initiation.get(REMITTANCE_INFORMATION)) &&
                                    isNotNull(initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE)) ?
                                    initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE).getAsString() :
                                    null
                    );

                    if (isNotNull(data.get(CHARGES))) {

                        details.setCharges(
                                decodeCharges(
                                        data.getAsJsonArray(CHARGES),
                                        details.getInstructedAmount().getCurrency()
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
        return IntentType.PAYMENT_DOMESTIC_CONSENT;
    }

    private FRAmount decodeCharges(JsonArray chargesArray, String currency) {
        FRAmount charges = FRAmount.builder().build();
        Double amount = 0.0;
        for (JsonElement charge : chargesArray) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            amount += chargeAmount.get(AMOUNT).getAsDouble();
        }
        charges.setAmount(amount.toString());
        charges.setCurrency(currency);
        return charges;
    }

    private FRAmount decodeInstructedAmount(JsonObject instructedAmount) {
        FRAmount frAmount = FRAmount.builder().build();
        frAmount.setAmount(
                isNotNull(instructedAmount.get(AMOUNT)) ? instructedAmount.get(AMOUNT).getAsString() : null
        );
        frAmount.setCurrency(
                isNotNull(instructedAmount.get(CURRENCY)) ? instructedAmount.get(CURRENCY).getAsString() : null
        );
        return frAmount;
    }
}
