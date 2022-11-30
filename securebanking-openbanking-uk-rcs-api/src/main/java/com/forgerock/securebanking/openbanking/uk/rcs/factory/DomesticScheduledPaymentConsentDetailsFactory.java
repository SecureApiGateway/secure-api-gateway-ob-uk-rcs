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
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticScheduledPaymentConsentDetails;
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
 * Domestic Scheduled Payment consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
public class DomesticScheduledPaymentConsentDetailsFactory implements ConsentDetailsFactory<DomesticScheduledPaymentConsentDetails> {

    private final DomesticScheduledPaymentConsentDetails details;

    @Autowired
    public DomesticScheduledPaymentConsentDetailsFactory(DomesticScheduledPaymentConsentDetails details) {
        this.details = details;
    }

    @Override
    public DomesticScheduledPaymentConsentDetails decode(JsonObject json) {
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

                    if (isNotNull(initiation.get(INSTRUCTED_AMOUNT))) {
                        setInstructedAmount(initiation.getAsJsonObject(INSTRUCTED_AMOUNT));
                    }

                    details.setPaymentReference(
                            isNotNull(initiation.get(REMITTANCE_INFORMATION)) &&
                                    isNotNull(initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE))
                                    ? initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE).getAsString()
                                    : null
                    );

                    details.setPaymentDate(
                            isNotNull(initiation.get(REQUESTED_EXECUTION_DATETIME))
                                    ? Instant.parse(initiation.get(REQUESTED_EXECUTION_DATETIME).getAsString()).toDateTime()
                                    : null
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
        details.getCharges().setCurrency(details.getInstructedAmount().getCurrency());
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
}
