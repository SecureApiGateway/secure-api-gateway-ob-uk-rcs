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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
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

import java.math.BigDecimal;
import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.AMOUNT;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.CURRENCY;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Models the consent data for a file payment.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class FilePaymentConsentDetails extends ConsentDetails {

    private FRWriteFileDataInitiation filePayment;
    private List<FRAccountWithBalance> accounts;
    private FRAmount charges;
    private DateTime expiredDate;
    private String fileReference;
    private FRAmount totalAmount;
    private String numberOfTransactions;
    private BigDecimal controlSum;
    private String paymentReference;
    private String requestedExecutionDateTime;

    public void setFilePayment(FRWriteFileDataInitiation filePayment) {
        this.filePayment = filePayment;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_FILE_CONSENT;
    }

    public void setCharges(JsonArray charges) {
        if (!isNotNull(charges)) {
            this.charges = null;
        } else {
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
