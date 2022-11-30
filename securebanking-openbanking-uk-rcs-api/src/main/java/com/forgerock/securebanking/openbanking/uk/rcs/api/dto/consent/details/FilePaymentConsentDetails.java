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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteFileDataInitiation;
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

import java.math.BigDecimal;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Models the consent data for a file payment.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class FilePaymentConsentDetails extends ConsentDetails {

    private FRWriteFileDataInitiation filePayment;
    private FRAmount charges;
    private DateTime expiredDate;
    private String fileReference;
    private FRAmount totalAmount;
    private String numberOfTransactions;
    private BigDecimal controlSum;
    private String paymentReference;
    private String requestedExecutionDateTime;

    @Override
    public FilePaymentConsentDetails getInstance() {
        return new FilePaymentConsentDetails();
    }

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
    }

    public void setFilePayment(FRWriteFileDataInitiation filePayment) {
        this.filePayment = filePayment;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_FILE_CONSENT;
    }

    public void setCharges(JsonArray charges) {
        this.charges = new FRAmount();
        Double amount = 0.0;

        for (JsonElement charge : charges) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            amount += chargeAmount.get(AMOUNT).getAsDouble();
        }

        String currency = charges.get(0).getAsJsonObject().getAsJsonObject(AMOUNT).get(CURRENCY).getAsString();

        this.charges.setAmount(amount.toString());
        this.charges.setCurrency(currency);
    }

    public void setFilePayment(
            JsonElement numberOfTransactions,
            JsonElement controlSum,
            JsonElement requestedExecutionDateTime,
            JsonElement fileReference
    ) {
        FRWriteFileDataInitiation filePaymentData = new FRWriteFileDataInitiation();

        if (isNotNull(numberOfTransactions)) {
            filePaymentData.setNumberOfTransactions(numberOfTransactions.getAsString());
        }

        if (isNotNull(controlSum)) {
            filePaymentData.setControlSum(controlSum.getAsBigDecimal());
        }

        if (isNotNull(requestedExecutionDateTime)) {
            filePaymentData.setRequestedExecutionDateTime(
                    Instant.parse(requestedExecutionDateTime.getAsString()).toDateTime()
            );
        }

        if (isNotNull(fileReference)) {
            filePaymentData.setFileReference(fileReference.getAsString());
        }

        this.filePayment = filePaymentData;
    }

}
