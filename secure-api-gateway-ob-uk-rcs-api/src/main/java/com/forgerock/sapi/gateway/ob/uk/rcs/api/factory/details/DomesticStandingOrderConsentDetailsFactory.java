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
package com.forgerock.sapi.gateway.ob.uk.rcs.api.factory.details;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticStandingOrderConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.factory.details.decoder.FRAccountIdentifierDecoder;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.json.utils.JsonUtilValidation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticStandingOrderDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.IntentType;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.forgerock.FRFrequency;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import static com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.sapi.gateway.ob.uk.rcs.api.json.utils.JsonUtilValidation.isNotNull;
import static java.util.Objects.requireNonNull;

/**
 * Domestic Standing Order consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
@Slf4j
public class DomesticStandingOrderConsentDetailsFactory implements ConsentDetailsFactory<DomesticStandingOrderConsentDetails> {

    private final FRAccountIdentifierDecoder accountIdentifierDecoder;

    public DomesticStandingOrderConsentDetailsFactory(FRAccountIdentifierDecoder accountIdentifierDecoder) {
        this.accountIdentifierDecoder = accountIdentifierDecoder;
    }

    @Override
    public DomesticStandingOrderConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        DomesticStandingOrderConsentDetails details = new DomesticStandingOrderConsentDetails();
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (JsonUtilValidation.isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (JsonUtilValidation.isNotNull(data.get(INITIATION))) {
                    JsonObject initiation = data.getAsJsonObject(INITIATION);

                    details.setPaymentReference(
                            JsonUtilValidation.isNotNull(initiation.get(REFERENCE)) ?
                                    initiation.get(REFERENCE).getAsString() :
                                    null
                    );
                    details.setInitiation(decodeDataInitiation(initiation));

                    if (JsonUtilValidation.isNotNull(data.get(CHARGES))) {
                        details.setCharges(
                                decodeCharges(
                                        data.getAsJsonArray(CHARGES),
                                        details.getInitiation().getFirstPaymentAmount().getCurrency()
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
        return IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT;
    }

    private FRAmount decodeCharges(JsonArray chargesArray, String currency) {
        FRAmount charges = new FRAmount();
        Double amount = 0.0;
        for (JsonElement charge : chargesArray) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            amount += chargeAmount.get(AMOUNT).getAsDouble();
        }
        charges.setAmount(amount.toString());
        charges.setCurrency(currency);
        return charges;
    }

    private FRWriteDomesticStandingOrderDataInitiation decodeDataInitiation(JsonObject initiation) {
        log.debug("{}.{}.{}: {}", OB_INTENT_OBJECT, DATA, INITIATION, initiation);

        FRWriteDomesticStandingOrderDataInitiation domesticStandingOrderDataInitiation = new FRWriteDomesticStandingOrderDataInitiation();
        if(isNotNull(initiation.get(DEBTOR_ACCOUNT))) {
            domesticStandingOrderDataInitiation.setDebtorAccount(accountIdentifierDecoder.decode(initiation.getAsJsonObject(DEBTOR_ACCOUNT)));
        }
        if (JsonUtilValidation.isNotNull(initiation.get(CREDITOR_ACCOUNT))) {
            domesticStandingOrderDataInitiation.setCreditorAccount(accountIdentifierDecoder.decode(initiation.getAsJsonObject(CREDITOR_ACCOUNT)));
        }
        if (JsonUtilValidation.isNotNull(initiation.get(FINAL_PAYMENT_DATETIME))) {
            domesticStandingOrderDataInitiation.setFinalPaymentDateTime(
                    Instant.parse(initiation.get(FINAL_PAYMENT_DATETIME).getAsString()).toDateTime()
            );
        }
        if (JsonUtilValidation.isNotNull(initiation.get(FINAL_PAYMENT_AMOUNT))) {
            JsonObject finalPaymentAmount = initiation.getAsJsonObject(FINAL_PAYMENT_AMOUNT);
            FRAmount frFinalPaymentAmount = new FRAmount();
            frFinalPaymentAmount.setAmount(
                    JsonUtilValidation.isNotNull(initiation.getAsJsonObject(FINAL_PAYMENT_AMOUNT).get(AMOUNT)) ?
                            initiation.getAsJsonObject(FINAL_PAYMENT_AMOUNT).get(AMOUNT).getAsString() :
                            null
            );
            frFinalPaymentAmount.setCurrency(
                    JsonUtilValidation.isNotNull(finalPaymentAmount.get(CURRENCY)) ? finalPaymentAmount.get(CURRENCY).getAsString() : null
            );
            domesticStandingOrderDataInitiation.setFinalPaymentAmount(frFinalPaymentAmount);
        }

        if (JsonUtilValidation.isNotNull(initiation.get(FIRST_PAYMENT_DATETIME))) {
            domesticStandingOrderDataInitiation.setFirstPaymentDateTime(
                    Instant.parse(initiation.get(FIRST_PAYMENT_DATETIME).getAsString()).toDateTime()
            );
        }
        if (JsonUtilValidation.isNotNull(initiation.get(FIRST_PAYMENT_AMOUNT))) {
            JsonObject firstPaymentAmount = initiation.getAsJsonObject(FIRST_PAYMENT_AMOUNT);
            FRAmount frFirstPaymentAmount = new FRAmount();
            frFirstPaymentAmount.setAmount(
                    JsonUtilValidation.isNotNull(firstPaymentAmount.get(AMOUNT)) ? firstPaymentAmount.get(AMOUNT).getAsString() : null
            );
            frFirstPaymentAmount.setCurrency(
                    JsonUtilValidation.isNotNull(firstPaymentAmount.get(CURRENCY)) ? firstPaymentAmount.get(CURRENCY).getAsString() : null
            );
            domesticStandingOrderDataInitiation.setFirstPaymentAmount(frFirstPaymentAmount);
        }

        if (JsonUtilValidation.isNotNull(initiation.get(RECURRING_PAYMENT_DATETIME))) {
            domesticStandingOrderDataInitiation.setRecurringPaymentDateTime(
                    Instant.parse(initiation.get(RECURRING_PAYMENT_DATETIME).getAsString()).toDateTime()
            );
        }
        if (JsonUtilValidation.isNotNull(initiation.get(RECURRING_PAYMENT_AMOUNT))) {
            JsonObject recurringPaymentAmount = initiation.getAsJsonObject(RECURRING_PAYMENT_AMOUNT);
            FRAmount frRecurringPaymentAmount = new FRAmount();
            frRecurringPaymentAmount.setAmount(
                    JsonUtilValidation.isNotNull(recurringPaymentAmount.get(AMOUNT))
                            ? recurringPaymentAmount.get(AMOUNT).getAsString() : null
            );
            frRecurringPaymentAmount.setCurrency(
                    JsonUtilValidation.isNotNull(recurringPaymentAmount.get(CURRENCY))
                            ? recurringPaymentAmount.get(CURRENCY).getAsString() : null
            );
            domesticStandingOrderDataInitiation.setRecurringPaymentAmount(frRecurringPaymentAmount);
        }

        if (JsonUtilValidation.isNotNull(initiation.get(FREQUENCY))) {
            String frequencyType = initiation.get(FREQUENCY).getAsString();
            FRFrequency frFrequency = new FRFrequency(frequencyType);
            String sentence = frFrequency.getSentence();
            domesticStandingOrderDataInitiation.setFrequency(sentence);
        }

        return domesticStandingOrderDataInitiation;
    }
}
