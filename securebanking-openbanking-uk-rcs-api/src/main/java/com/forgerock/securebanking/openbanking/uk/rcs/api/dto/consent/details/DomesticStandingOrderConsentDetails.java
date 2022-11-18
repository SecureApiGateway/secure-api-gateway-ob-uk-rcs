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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticStandingOrderDataInitiation;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.forgerock.FRFrequency;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joda.time.Instant;

import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.AMOUNT;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.CURRENCY;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Models the consent data for a domestic standing order.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class DomesticStandingOrderConsentDetails extends ConsentDetails {
    private FRWriteDomesticStandingOrderDataInitiation standingOrder;
    private List<FRAccountWithBalance> accounts;
    private String paymentReference;
    private FRAmount charges;

    public void setStandingOrder(FRWriteDomesticStandingOrderDataInitiation standingOrder) {
        this.standingOrder = standingOrder;
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

            this.charges.setCurrency(standingOrder.getFirstPaymentAmount().getCurrency());
            this.charges.setAmount(amount.toString());
        }
    }

    public void setStandingOrder(
            JsonElement finalPaymentDateTime,
            JsonObject finalPaymentAmount,
            JsonElement firstPaymentDateTime,
            JsonObject firstPaymentAmount,
            JsonElement recurringPaymentDateTime,
            JsonObject recurringPaymentAmount,
            JsonElement frequency
    ) {
        FRWriteDomesticStandingOrderDataInitiation standingOrderData = new FRWriteDomesticStandingOrderDataInitiation();

        if (isNotNull(finalPaymentDateTime)) {
            standingOrderData.setFinalPaymentDateTime(Instant.parse(finalPaymentDateTime.getAsString()).toDateTime());
        }
        if (isNotNull(finalPaymentAmount)) {
            FRAmount frFinalPaymentAmount = new FRAmount();
            frFinalPaymentAmount.setAmount(
                    isNotNull(finalPaymentAmount.get(AMOUNT)) ? finalPaymentAmount.get(AMOUNT).getAsString() : null
            );
            frFinalPaymentAmount.setCurrency(
                    isNotNull(finalPaymentAmount.get(CURRENCY)) ? finalPaymentAmount.get(CURRENCY).getAsString() : null
            );
            standingOrderData.setFinalPaymentAmount(frFinalPaymentAmount);
        }

        if (isNotNull(firstPaymentDateTime)) {
            standingOrderData.setFirstPaymentDateTime(Instant.parse(firstPaymentDateTime.getAsString()).toDateTime());
        }
        if (isNotNull(firstPaymentAmount)) {
            FRAmount frFirstPaymentAmount = new FRAmount();
            frFirstPaymentAmount.setAmount(
                    isNotNull(firstPaymentAmount.get(AMOUNT)) ? firstPaymentAmount.get(AMOUNT).getAsString() : null
            );
            frFirstPaymentAmount.setCurrency(
                    isNotNull(firstPaymentAmount.get(CURRENCY)) ? firstPaymentAmount.get(CURRENCY).getAsString() : null
            );
            standingOrderData.setFirstPaymentAmount(frFirstPaymentAmount);
        }

        if (isNotNull(recurringPaymentDateTime)) {
            standingOrderData.setRecurringPaymentDateTime(
                    Instant.parse(recurringPaymentDateTime.getAsString()).toDateTime()
            );
        }
        if (isNotNull(recurringPaymentAmount)) {
            FRAmount frRecurringPaymentAmount = new FRAmount();
            frRecurringPaymentAmount.setAmount(
                    isNotNull(recurringPaymentAmount.get(AMOUNT))
                            ? recurringPaymentAmount.get(AMOUNT).getAsString() : null
            );
            frRecurringPaymentAmount.setCurrency(
                    isNotNull(recurringPaymentAmount.get(CURRENCY))
                            ? recurringPaymentAmount.get(CURRENCY).getAsString() : null
            );
            standingOrderData.setRecurringPaymentAmount(frRecurringPaymentAmount);
        }

        if (isNotNull(frequency)) {
            String frequencyType = frequency.getAsString();
            FRFrequency frFrequency = new FRFrequency(frequencyType);
            String sentence = frFrequency.getSentence();
            standingOrderData.setFrequency(sentence);
        }

        this.standingOrder = standingOrderData;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT;
    }
}
