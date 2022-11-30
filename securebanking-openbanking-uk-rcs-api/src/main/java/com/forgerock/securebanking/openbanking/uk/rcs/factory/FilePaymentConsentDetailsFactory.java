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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteFileDataInitiation;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.FilePaymentConsentDetails;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.CHARGES;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.json.utils.JsonUtilValidation.isNotNull;

/**
 * File Payment consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
public class FilePaymentConsentDetailsFactory implements ConsentDetailsFactory<FilePaymentConsentDetails>{
    private final FilePaymentConsentDetails details;

    @Autowired
    public FilePaymentConsentDetailsFactory(FilePaymentConsentDetails details) {
        this.details = details;
    }

    @Override
    public FilePaymentConsentDetails decode(JsonObject json) {
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (isNotNull(data.get(INITIATION))) {

                    JsonObject initiation = data.getAsJsonObject(INITIATION);

                    setFilePayment(
                            isNotNull(initiation.get(NUMBER_OF_TRANSACTIONS))
                                    ? initiation.get(NUMBER_OF_TRANSACTIONS) : null,
                            isNotNull(initiation.get(CONTROL_SUM)) ? initiation.get(CONTROL_SUM) : null,
                            isNotNull(initiation.get(REQUESTED_EXECUTION_DATETIME))
                                    ? initiation.get(REQUESTED_EXECUTION_DATETIME) : null,
                            isNotNull(initiation.get(FILE_REFERENCE)) ? initiation.get(FILE_REFERENCE) : null
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

    private void setCharges(JsonArray charges) {
        details.setCharges(FRAmount.builder().build());
        Double amount = 0.0;

        for (JsonElement charge : charges) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            amount += chargeAmount.get(AMOUNT).getAsDouble();
        }

        String currency = charges.get(0).getAsJsonObject().getAsJsonObject(AMOUNT).get(CURRENCY).getAsString();

        details.getCharges().setAmount(amount.toString());
        details.getCharges().setCurrency(currency);
    }

    private void setFilePayment(
            JsonElement numberOfTransactions,
            JsonElement controlSum,
            JsonElement requestedExecutionDateTime,
            JsonElement fileReference
    ) {
        FRWriteFileDataInitiation fileDataInitiation = new FRWriteFileDataInitiation();

        if (isNotNull(numberOfTransactions)) {
            fileDataInitiation.setNumberOfTransactions(numberOfTransactions.getAsString());
        }

        if (isNotNull(controlSum)) {
            fileDataInitiation.setControlSum(controlSum.getAsBigDecimal());
        }

        if (isNotNull(requestedExecutionDateTime)) {
            fileDataInitiation.setRequestedExecutionDateTime(
                    Instant.parse(requestedExecutionDateTime.getAsString()).toDateTime()
            );
        }

        if (isNotNull(fileReference)) {
            fileDataInitiation.setFileReference(fileReference.getAsString());
        }

        details.setFileDataInitiation(fileDataInitiation);
    }
}
