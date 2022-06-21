/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticScheduledPaymentsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticStandingOrderPaymentsConsentDetails;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Converter class to map {@link JsonObject} to {@link DomesticScheduledPaymentsConsentDetails}
 */
@Slf4j
@NoArgsConstructor
public class DomesticStandingOrderPaymentConsentDetailsConverter {

    private static volatile DomesticStandingOrderPaymentConsentDetailsConverter instance;
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");

    /*
     * Double checked locking principle to ensure that only one instance 'DomesticPaymentConsentDetailsConverter' is created
     */
    public static DomesticStandingOrderPaymentConsentDetailsConverter getInstance() {
        if (instance == null) {
            synchronized (DomesticStandingOrderPaymentConsentDetailsConverter.class) {
                if (instance == null) {
                    instance = new DomesticStandingOrderPaymentConsentDetailsConverter();
                }
            }
        }
        return instance;
    }

    public DomesticStandingOrderPaymentsConsentDetails mapping(JsonObject consentDetails) {
        DomesticStandingOrderPaymentsConsentDetails details = new DomesticStandingOrderPaymentsConsentDetails();

        details.setMerchantName(consentDetails.get("oauth2ClientName") != null ?
                consentDetails.get("oauth2ClientName").getAsString() :
                null);

        if (!isNotNull(consentDetails.getAsJsonObject("data"))) {
            details.setPaymentReference(null);
        } else if (isNotNull(consentDetails.getAsJsonObject("data").get("Initiation"))) {
            JsonObject initiation = consentDetails.getAsJsonObject("data").getAsJsonObject("Initiation");

            details.setPaymentReference(isNotNull(initiation.get("Reference")) ?
                    initiation.get("Reference").getAsString() : null);

            details.setStandingOrder(
                    initiation.get("FinalPaymentDateTime"),
                    initiation.getAsJsonObject("FinalPaymentAmount"),
                    initiation.get("FirstPaymentDateTime"),
                    initiation.getAsJsonObject("FirstPaymentAmount"),
                    initiation.get("RecurringPaymentDateTime"),
                    initiation.getAsJsonObject("RecurringPaymentAmount"),
                    initiation.get("Frequency")
            );

        }
        return details;
    }

    public final DomesticStandingOrderPaymentsConsentDetails toDomesticStandingOrderPaymentConsentDetails(JsonObject consentDetails) {
        return mapping(consentDetails);
    }
}
