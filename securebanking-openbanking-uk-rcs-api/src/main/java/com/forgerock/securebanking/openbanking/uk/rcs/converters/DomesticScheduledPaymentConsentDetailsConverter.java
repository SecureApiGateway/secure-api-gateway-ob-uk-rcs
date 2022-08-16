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
public class DomesticScheduledPaymentConsentDetailsConverter {

    private static volatile DomesticScheduledPaymentConsentDetailsConverter instance;
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");

    /*
     * Double checked locking principle to ensure that only one instance 'DomesticPaymentConsentDetailsConverter' is created
     */
    public static DomesticScheduledPaymentConsentDetailsConverter getInstance() {
        if (instance == null) {
            synchronized (DomesticScheduledPaymentConsentDetailsConverter.class) {
                if (instance == null) {
                    instance = new DomesticScheduledPaymentConsentDetailsConverter();
                }
            }
        }
        return instance;
    }

    public DomesticScheduledPaymentConsentDetails mapping(JsonObject consentDetails) {
        DomesticScheduledPaymentConsentDetails details = new DomesticScheduledPaymentConsentDetails();

        details.setMerchantName(isNotNull(consentDetails.get("oauth2ClientName")) ?
                consentDetails.get("oauth2ClientName").getAsString() :
                null);

        if (!isNotNull(consentDetails.get("data"))) {
            details.setInstructedAmount(null);
            details.setPaymentReference(null);
            details.setPaymentDate(null);
        } else {
            JsonObject data = consentDetails.getAsJsonObject("data");

            if (isNotNull(data.get("Initiation"))) {
                JsonObject initiation = data.getAsJsonObject("Initiation");

                details.setInstructedAmount(isNotNull(initiation.get("InstructedAmount")) ?
                        initiation.getAsJsonObject("InstructedAmount") :
                        null);

                details.setPaymentReference(isNotNull(initiation.get("RemittanceInformation")) &&
                        isNotNull(initiation.getAsJsonObject("RemittanceInformation").get("Reference")) ?
                        initiation.getAsJsonObject("RemittanceInformation").get("Reference").getAsString() :
                        null);

                details.setPaymentDate(isNotNull(initiation.get("RequestedExecutionDateTime")) ?
                        DATE_TIME_FORMATTER.parseDateTime(initiation.get("RequestedExecutionDateTime").getAsString()) :
                        null);

                details.setCharges(isNotNull(data.get("Charges")) ?
                        data.getAsJsonArray("Charges") :
                        null);
            }
        }
        return details;
    }

    public final DomesticScheduledPaymentConsentDetails toDomesticScheduledPaymentConsentDetails(JsonObject consentDetails) {
        return mapping(consentDetails);
    }
}
