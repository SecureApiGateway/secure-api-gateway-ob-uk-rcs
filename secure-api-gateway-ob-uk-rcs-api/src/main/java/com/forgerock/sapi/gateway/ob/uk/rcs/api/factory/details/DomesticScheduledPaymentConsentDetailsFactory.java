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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticScheduledDataInitiation.FRWriteDomesticScheduledDataInitiationBuilder;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticScheduledPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.factory.details.decoder.FRAccountIdentifierDecoder;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.json.utils.JsonUtilValidation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticScheduledDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.IntentType;
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
 * Domestic Scheduled Payment consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
@Slf4j
public class DomesticScheduledPaymentConsentDetailsFactory implements ConsentDetailsFactory<DomesticScheduledPaymentConsentDetails> {

    private final FRAccountIdentifierDecoder accountIdentifierDecoder;

    public DomesticScheduledPaymentConsentDetailsFactory(FRAccountIdentifierDecoder accountIdentifierDecoder) {
        this.accountIdentifierDecoder = accountIdentifierDecoder;
    }

    @Override
    public DomesticScheduledPaymentConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        DomesticScheduledPaymentConsentDetails details = new DomesticScheduledPaymentConsentDetails();
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (JsonUtilValidation.isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (JsonUtilValidation.isNotNull(data.get(INITIATION))) {
                    JsonObject initiation = data.getAsJsonObject(INITIATION);

                    details.setInitiation(decodeDataInitiation(initiation));

                    if (JsonUtilValidation.isNotNull(initiation.get(INSTRUCTED_AMOUNT))) {
                        details.setInstructedAmount(
                                decodeInstructedAmount(initiation.getAsJsonObject(INSTRUCTED_AMOUNT))
                        );
                    }

                    details.setPaymentReference(
                            JsonUtilValidation.isNotNull(initiation.get(REMITTANCE_INFORMATION)) &&
                                    JsonUtilValidation.isNotNull(initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE))
                                    ? initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE).getAsString()
                                    : null
                    );

                    details.setPaymentDate(
                            JsonUtilValidation.isNotNull(initiation.get(REQUESTED_EXECUTION_DATETIME))
                                    ? Instant.parse(initiation.get(REQUESTED_EXECUTION_DATETIME).getAsString()).toDateTime()
                                    : null
                    );

                    if (JsonUtilValidation.isNotNull(data.get(CHARGES))) {
                        details.setCharges(
                                decodeCharges(data.getAsJsonArray(CHARGES), details.getInstructedAmount().getCurrency())
                        );
                    }
                }
            }
        }
        return details;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT;
    }

    private FRWriteDomesticScheduledDataInitiation decodeDataInitiation(JsonObject initiation) {
        log.debug("{}.{}.{}: {}", OB_INTENT_OBJECT, DATA, INITIATION, initiation);

        final FRWriteDomesticScheduledDataInitiationBuilder initiationBuilder = FRWriteDomesticScheduledDataInitiation.builder();
        if(isNotNull(initiation.get(DEBTOR_ACCOUNT))) {
            initiationBuilder.debtorAccount(accountIdentifierDecoder.decode(initiation.getAsJsonObject(DEBTOR_ACCOUNT)));
        }
        if (JsonUtilValidation.isNotNull(initiation.get(CREDITOR_ACCOUNT))) {
            initiationBuilder.creditorAccount(accountIdentifierDecoder.decode(initiation.getAsJsonObject(CREDITOR_ACCOUNT)));
        }
        return initiationBuilder.build();
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

    private FRAmount decodeInstructedAmount(JsonObject instructedAmount) {
        FRAmount frAmount = new FRAmount();
        frAmount.setAmount(
                JsonUtilValidation.isNotNull(instructedAmount.get(AMOUNT)) ? instructedAmount.get(AMOUNT).getAsString() : null
        );
        frAmount.setCurrency(
                JsonUtilValidation.isNotNull(instructedAmount.get(CURRENCY)) ? instructedAmount.get(CURRENCY).getAsString() : null
        );
        return frAmount;
    }
}
