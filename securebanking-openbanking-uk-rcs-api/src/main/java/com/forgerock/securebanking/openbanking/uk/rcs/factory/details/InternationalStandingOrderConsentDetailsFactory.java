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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAccountIdentifier;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAmount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrderDataInitiation;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.forgerock.FRFrequency;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.InternationalStandingOrderConsentDetails;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.json.utils.JsonUtilValidation.isNotNull;
import static java.util.Objects.requireNonNull;

/**
 * International Standing Order consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
@Slf4j
public class InternationalStandingOrderConsentDetailsFactory implements ConsentDetailsFactory<InternationalStandingOrderConsentDetails> {

    @Override
    public InternationalStandingOrderConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        InternationalStandingOrderConsentDetails details = new InternationalStandingOrderConsentDetails();
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (isNotNull(data.get(INITIATION))) {

                    JsonObject initiation = data.getAsJsonObject(INITIATION);

                    details.setPaymentReference(
                            isNotNull(initiation.get(REFERENCE)) ?
                                    initiation.get(REFERENCE).getAsString() :
                                    null
                    );

                    details.setCurrencyOfTransfer(
                            isNotNull(initiation.get(CURRENCY_OF_TRANSFER)) ?
                                    initiation.get(CURRENCY_OF_TRANSFER).getAsString() :
                                    null
                    );

                    details.setInitiation(decodeDataInitiation(initiation));

                    if (isNotNull(data.get(CHARGES))) {
                        details.setCharges(
                                decodeCharges(
                                        data.getAsJsonArray(CHARGES),
                                        details.getInitiation().getInstructedAmount().getCurrency()
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
        return IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT;
    }

    private FRAmount decodeCharges(JsonArray chargesArray, String currency) {
        FRAmount charges = new FRAmount();
        Double amount = 0.0;

        for (JsonElement charge : chargesArray) {
            JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject(AMOUNT);
            amount += chargeAmount.get(AMOUNT).getAsDouble();
        }

        charges.setCurrency(currency);
        charges.setAmount(amount.toString());
        return charges;
    }

    private FRWriteInternationalStandingOrderDataInitiation decodeDataInitiation(JsonObject initiation) {
        log.debug("{}.{}.{}: {}", OB_INTENT_OBJECT, DATA, INITIATION, initiation);

        FRWriteInternationalStandingOrderDataInitiation internationalStandingOrderDataInitiation = new FRWriteInternationalStandingOrderDataInitiation();

        if (isNotNull(initiation.get(DEBTOR_ACCOUNT))) {
            JsonObject debtorAccount = initiation.getAsJsonObject(DEBTOR_ACCOUNT);
            internationalStandingOrderDataInitiation.setDebtorAccount(
                    FRAccountIdentifier.builder()
                            .identification(debtorAccount.get(IDENTIFICATION).getAsString())
                            .name(debtorAccount.get(NAME).getAsString())
                            .schemeName(debtorAccount.get(SCHEME_NAME).getAsString())
                            .secondaryIdentification(
                                    isNotNull(debtorAccount.get(SECONDARY_IDENTIFICATION)) ?
                                            debtorAccount.get(SECONDARY_IDENTIFICATION).getAsString() :
                                            null
                            )
                            .build()
            );
        }

        if (isNotNull(initiation.get(FIRST_PAYMENT_DATETIME))) {
            internationalStandingOrderDataInitiation.setFirstPaymentDateTime(
                    Instant.parse(initiation.get(FIRST_PAYMENT_DATETIME).getAsString()).toDateTime()
            );
        }

        if (isNotNull(initiation.get(FINAL_PAYMENT_DATETIME))) {
            internationalStandingOrderDataInitiation.setFinalPaymentDateTime(
                    Instant.parse(initiation.get(FINAL_PAYMENT_DATETIME).getAsString()).toDateTime()
            );
        }

        if (isNotNull(initiation.getAsJsonObject(INSTRUCTED_AMOUNT))) {
            JsonObject instructedAmount = initiation.getAsJsonObject(INSTRUCTED_AMOUNT);
            FRAmount frInstructedAmount = new FRAmount();
            frInstructedAmount.setAmount(
                    isNotNull(instructedAmount.get(AMOUNT)) ? instructedAmount.get(AMOUNT).getAsString() : null
            );
            frInstructedAmount.setCurrency(
                    isNotNull(instructedAmount.get(CURRENCY)) ? instructedAmount.get(CURRENCY).getAsString() : null
            );
            internationalStandingOrderDataInitiation.setInstructedAmount(frInstructedAmount);
        }

        if (isNotNull(initiation.get(FREQUENCY))) {
            String frequencyType = initiation.get(FREQUENCY).getAsString();
            FRFrequency frFrequency = new FRFrequency(frequencyType);
            String sentence = frFrequency.getSentence();
            internationalStandingOrderDataInitiation.setFrequency(sentence);
        }

        return internationalStandingOrderDataInitiation;
    }
}
