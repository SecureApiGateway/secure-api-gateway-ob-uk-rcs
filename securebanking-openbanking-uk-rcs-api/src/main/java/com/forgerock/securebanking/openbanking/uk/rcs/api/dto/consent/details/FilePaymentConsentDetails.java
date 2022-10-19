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
import com.forgerock.securebanking.openbanking.uk.rcs.converters.DomesticStandingOrderConsentDetailsConverter;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.FilePaymentConsentDetailsConverter;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joda.time.DateTime;

import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.FilePaymentConsentDetailsConverter.DATE_TIME_FORMATTER;
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
    private String merchantName;
    private String pispName;
    private DateTime expiredDate;
    private String fileReference;
    private FRAmount totalAmount;
    private String numberOfTransactions;
    private String paymentReference;
    private DateTime requestedExecutionDateTime;

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
                JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject("Amount");
                amount += chargeAmount.get("Amount").getAsDouble();
            }

            String currency = charges.get(0).getAsJsonObject().get("Currency").getAsString();
            
            this.charges.setAmount(amount.toString());
            this.charges.setCurrency(currency);
        }
    }

    public void setFilePayment(JsonElement numberOfTransactions, JsonElement controlSum, JsonElement requestedExecutionDateTime) {
        FRWriteFileDataInitiation filePaymentData = new FRWriteFileDataInitiation();

        if (isNotNull(numberOfTransactions)) {
            filePaymentData.setNumberOfTransactions(numberOfTransactions.getAsString());
        }

        if (isNotNull(controlSum)) {
            filePaymentData.setControlSum(controlSum.getAsBigDecimal());
        }

        if (isNotNull(requestedExecutionDateTime)) {
            filePaymentData.setRequestedExecutionDateTime(FilePaymentConsentDetailsConverter.DATE_TIME_FORMATTER.parseDateTime(String.valueOf(requestedExecutionDateTime.getAsBigDecimal())));
        }

        this.filePayment = filePaymentData;
    }

}
