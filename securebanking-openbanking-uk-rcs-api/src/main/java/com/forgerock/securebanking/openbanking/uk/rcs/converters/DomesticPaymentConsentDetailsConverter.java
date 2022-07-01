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

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticPaymentsConsentDetails;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Converter class to map {@link JsonObject} to {@link DomesticPaymentsConsentDetails}
 */
@Slf4j
@NoArgsConstructor
public class DomesticPaymentConsentDetailsConverter {

    private static volatile DomesticPaymentConsentDetailsConverter instance;

    /*
     * Double checked locking principle to ensure that only one instance 'DomesticPaymentConsentDetailsConverter' is created
     */
    public static DomesticPaymentConsentDetailsConverter getInstance() {
        if (instance == null) {
            synchronized (DomesticPaymentConsentDetailsConverter.class) {
                if (instance == null) {
                    instance = new DomesticPaymentConsentDetailsConverter();
                }
            }
        }
        return instance;
    }

    public DomesticPaymentsConsentDetails mapping(JsonObject consentDetails) {
        DomesticPaymentsConsentDetails details = new DomesticPaymentsConsentDetails();

        details.setMerchantName(isNotNull(consentDetails.get("oauth2ClientName")) ?
                consentDetails.get("oauth2ClientName").getAsString() :
                null);

        if (!isNotNull(consentDetails.get("data"))) {
            details.setInstructedAmount(null);
            details.setPaymentReference(null);
        } else if (isNotNull(consentDetails.getAsJsonObject("data").get("Initiation"))) {
            JsonObject initiation = consentDetails.getAsJsonObject("data").getAsJsonObject("Initiation");

            details.setInstructedAmount(isNotNull(initiation.get("InstructedAmount")) ?
                    initiation.getAsJsonObject("InstructedAmount") :
                    null);

            details.setPaymentReference(isNotNull(initiation.get("RemittanceInformation")) &&
                    isNotNull(initiation.getAsJsonObject("RemittanceInformation").get("Reference")) ?
                    initiation.getAsJsonObject("RemittanceInformation").get("Reference").getAsString() :
                    null);
        }
        return details;
    }

    public final DomesticPaymentsConsentDetails toDomesticPaymentConsentDetails(JsonObject consentDetails) {
        return mapping(consentDetails);
    }
}
