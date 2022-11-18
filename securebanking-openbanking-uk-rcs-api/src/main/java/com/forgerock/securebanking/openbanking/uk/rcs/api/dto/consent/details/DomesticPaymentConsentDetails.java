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
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.AMOUNT;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.CURRENCY;
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
    private List<FRAccountWithBalance> accounts;
    private String paymentReference;
    private FRAmount charges;

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_DOMESTIC_CONSENT;
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

            this.charges.setCurrency(instructedAmount.getCurrency());
            this.charges.setAmount(amount.toString());
        }
    }

    public void setInstructedAmount(JsonObject instructedAmount) {
        if (instructedAmount == null)
            this.instructedAmount = null;
        else {
            this.instructedAmount = new FRAmount();
            this.instructedAmount.setAmount(
                    isNotNull(instructedAmount.get(AMOUNT)) ? instructedAmount.get(AMOUNT).getAsString() : null
            );
            this.instructedAmount.setCurrency(
                    isNotNull(instructedAmount.get(CURRENCY)) ? instructedAmount.get(CURRENCY).getAsString() : null
            );
        }
    }
}
