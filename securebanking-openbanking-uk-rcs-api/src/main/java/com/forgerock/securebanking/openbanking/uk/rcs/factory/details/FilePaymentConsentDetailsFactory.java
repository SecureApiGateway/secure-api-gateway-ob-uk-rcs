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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteFileDataInitiation;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.FilePaymentConsentDetails;
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
 * File Payment consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
public class FilePaymentConsentDetailsFactory implements ConsentDetailsFactory<FilePaymentConsentDetails> {

    @Override
    public FilePaymentConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        FilePaymentConsentDetails details = new FilePaymentConsentDetails();
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (isNotNull(data.get(INITIATION))) {

                    JsonObject initiation = data.getAsJsonObject(INITIATION);
                    details.setFileDataInitiation(
                            decodeFileDataInitiation(
                                    isNotNull(initiation.get(NUMBER_OF_TRANSACTIONS))
                                            ? initiation.get(NUMBER_OF_TRANSACTIONS) : null,
                                    isNotNull(initiation.get(CONTROL_SUM)) ? initiation.get(CONTROL_SUM) : null,
                                    isNotNull(initiation.get(REQUESTED_EXECUTION_DATETIME))
                                            ? initiation.get(REQUESTED_EXECUTION_DATETIME) : null,
                                    isNotNull(initiation.get(FILE_REFERENCE)) ? initiation.get(FILE_REFERENCE) : null
                            )
                    );

                    if (isNotNull(data.get(CHARGES))) {
                        details.setCharges(decodeCharges(data.getAsJsonArray(CHARGES)));
                    }
                }
            }
        }
        return details;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_FILE_CONSENT;
    }

    private FRAmount decodeCharges(JsonArray chargesArray) {
        FRAmount charges = new FRAmount();
        Double amount = 0.0;

        for (JsonElement charge : chargesArray) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            amount += chargeAmount.get(AMOUNT).getAsDouble();
        }

        String currency = chargesArray.get(0).getAsJsonObject().getAsJsonObject(AMOUNT).get(CURRENCY).getAsString();

        charges.setAmount(amount.toString());
        charges.setCurrency(currency);
        return charges;
    }

    private FRWriteFileDataInitiation decodeFileDataInitiation(
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

        return fileDataInitiation;
    }
}
