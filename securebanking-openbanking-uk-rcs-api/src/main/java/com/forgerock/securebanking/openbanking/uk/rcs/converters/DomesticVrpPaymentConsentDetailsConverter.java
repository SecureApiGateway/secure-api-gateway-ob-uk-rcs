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

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticVrpPaymentConsentDetails;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Converter class to map {@link JsonObject} to {@link DomesticVrpPaymentConsentDetails}
 */
@Slf4j
@NoArgsConstructor
public class DomesticVrpPaymentConsentDetailsConverter {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
    private static volatile DomesticVrpPaymentConsentDetailsConverter instance;

    /*
     * Double checked locking principle to ensure that only one instance 'DomesticVrpPaymentConsentDetailsConverter' is created
     */
    public static DomesticVrpPaymentConsentDetailsConverter getInstance() {
        if (instance == null) {
            synchronized (DomesticVrpPaymentConsentDetailsConverter.class) {
                if (instance == null) {
                    instance = new DomesticVrpPaymentConsentDetailsConverter();
                }
            }
        }
        return instance;
    }

    public DomesticVrpPaymentConsentDetails mapping(JsonObject consentDetails) {
        DomesticVrpPaymentConsentDetails details = new DomesticVrpPaymentConsentDetails();

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
                details.setRemittanceInformation(null);
                details.setDomesticVrpPayment(null);
            } else {
                JsonObject data = consentDataElement.getAsJsonObject();

                if (isNotNull(data.get("Initiation"))) {

                    /*JsonObject initiation = data.getAsJsonObject("Initiation");

                    details.setPaymentReference(isNotNull(initiation.getRemittanceInformation.get("Reference")) ?
                            initiation.get("Reference").getAsString() : null);

                    details.setDomesticVrpPayment();

                    details.setObDomesticVRPControlParameters(isNotNull(data.get("ControlParameters")) ?
                            data.getAsJsonObject("ControlParameters") :
                            null);*/
                }
            }
            return details;
        }
    }

    public final DomesticVrpPaymentConsentDetails toDomesticVrpPaymentConsentDetails
            (JsonObject consentDetails) {
        return mapping(consentDetails);
    }
}
