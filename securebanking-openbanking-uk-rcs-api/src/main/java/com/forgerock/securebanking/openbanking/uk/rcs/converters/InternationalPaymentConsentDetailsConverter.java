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
package com.forgerock.securebanking.openbanking.uk.rcs.converters;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.InternationalPaymentConsentDetails;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.OAUTH2_CLIENT_NAME;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Converter class to map {@link JsonObject} to {@link InternationalPaymentConsentDetails}
 */
@Slf4j
@NoArgsConstructor
public class InternationalPaymentConsentDetailsConverter {

    private static volatile InternationalPaymentConsentDetailsConverter instance;

    /*
     * Double check locking principle to ensure that only one instance 'DomesticPaymentConsentDetailsConverter' is created
     */
    public static InternationalPaymentConsentDetailsConverter getInstance() {
        if (instance == null) {
            synchronized (InternationalPaymentConsentDetailsConverter.class) {
                if (instance == null) {
                    instance = new InternationalPaymentConsentDetailsConverter();
                }
            }
        }
        return instance;
    }

    public InternationalPaymentConsentDetails mapping(JsonObject consentDetails) {
        InternationalPaymentConsentDetails details = new InternationalPaymentConsentDetails();

        if (!consentDetails.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = consentDetails.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (!isNotNull(consentDataElement)) {
                details.setPaymentReference(null);
                details.setCurrencyOfTransfer(null);
                details.setExchangeRateInformation(null);
                details.setInstructedAmount(null);
            } else {
                JsonObject data = consentDataElement.getAsJsonObject();

                details.setExchangeRateInformation(isNotNull(data.get(EXCHANGE_RATE_INFORMATION))
                        ? data.getAsJsonObject(EXCHANGE_RATE_INFORMATION)
                        : null
                );

                if (isNotNull(data.get(INITIATION))) {
                    JsonObject initiation = data.getAsJsonObject(INITIATION);

                    details.setPaymentReference(
                            isNotNull(initiation.get(REMITTANCE_INFORMATION)) && isNotNull(initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE))
                                    ? initiation.getAsJsonObject(REMITTANCE_INFORMATION).get(REFERENCE).getAsString()
                                    : null
                    );

                    details.setCurrencyOfTransfer(isNotNull(initiation.get(CURRENCY_OF_TRANSFER))
                            ? initiation.get(CURRENCY_OF_TRANSFER).getAsString()
                            : null
                    );

                    details.setInstructedAmount(isNotNull(initiation.get(INSTRUCTED_AMOUNT))
                            ? initiation.getAsJsonObject(INSTRUCTED_AMOUNT)
                            : null
                    );

                    details.setCharges(isNotNull(data.get(CHARGES)) ? data.getAsJsonArray(CHARGES) : null);
                }
            }
        }
        return details;
    }

    public final InternationalPaymentConsentDetails toInternationalPaymentConsentDetails(JsonObject consentDetails) {
        return mapping(consentDetails);
    }
}
