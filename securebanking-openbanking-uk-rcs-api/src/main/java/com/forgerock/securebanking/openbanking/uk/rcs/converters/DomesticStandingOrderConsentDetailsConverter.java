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

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticScheduledPaymentConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticStandingOrderConsentDetails;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Converter class to map {@link JsonObject} to {@link DomesticScheduledPaymentConsentDetails}
 */
@Slf4j
@NoArgsConstructor
public class DomesticStandingOrderConsentDetailsConverter {

    private static volatile DomesticStandingOrderConsentDetailsConverter instance;
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");

    /*
     * Double checked locking principle to ensure that only one instance 'DomesticPaymentConsentDetailsConverter' is created
     */
    public static DomesticStandingOrderConsentDetailsConverter getInstance() {
        if (instance == null) {
            synchronized (DomesticStandingOrderConsentDetailsConverter.class) {
                if (instance == null) {
                    instance = new DomesticStandingOrderConsentDetailsConverter();
                }
            }
        }
        return instance;
    }

    public DomesticStandingOrderConsentDetails mapping(JsonObject consentDetails) {
        DomesticStandingOrderConsentDetails details = new DomesticStandingOrderConsentDetails();

        details.setMerchantName(isNotNull(consentDetails.get("oauth2ClientName")) ?
                consentDetails.get("oauth2ClientName").getAsString() :
                null);

        if (!consentDetails.has("OBIntentObject")) {
            throw new IllegalStateException("Expected OBIntentObject field in json");
        } else {
            final JsonObject obIntentObject = consentDetails.get("OBIntentObject").getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get("Data");
            if (!isNotNull(consentDataElement)) {
                details.setPaymentReference(null);
                details.setStandingOrder(null);
            } else {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (isNotNull(data.get("Initiation"))) {
                    JsonObject initiation = data.getAsJsonObject("Initiation");

                    details.setPaymentReference(isNotNull(initiation.get("Reference")) ?
                            initiation.get("Reference").getAsString() : null);

                    details.setStandingOrder(
                            isNotNull(initiation.get("FinalPaymentDateTime")) ? initiation.get("FinalPaymentDateTime") : null,
                            isNotNull(initiation.get("FinalPaymentAmount")) ? initiation.getAsJsonObject("FinalPaymentAmount") : null,
                            isNotNull(initiation.get("FirstPaymentDateTime")) ? initiation.get("FirstPaymentDateTime") : null,
                            isNotNull(initiation.get("FirstPaymentAmount")) ? initiation.getAsJsonObject("FirstPaymentAmount") : null,
                            isNotNull(initiation.get("RecurringPaymentDateTime")) ? initiation.get("RecurringPaymentDateTime") : null,
                            isNotNull(initiation.get("RecurringPaymentAmount")) ? initiation.getAsJsonObject("RecurringPaymentAmount") : null,
                            initiation.get("Frequency")
                    );

                    details.setCharges(isNotNull(data.get("Charges")) ?
                            data.getAsJsonArray("Charges") :
                            null);
                }
            }
        }
        return details;
    }

    public final DomesticStandingOrderConsentDetails toDomesticStandingOrderConsentDetails(JsonObject
                                                                                                   consentDetails) {
        return mapping(consentDetails);
    }
}
