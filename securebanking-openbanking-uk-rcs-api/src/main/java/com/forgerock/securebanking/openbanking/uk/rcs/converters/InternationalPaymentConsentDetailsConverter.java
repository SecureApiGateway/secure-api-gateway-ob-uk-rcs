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
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Converter class to map {@link JsonObject} to {@link InternationalPaymentConsentDetails}
 */
@Slf4j
@NoArgsConstructor
public class InternationalPaymentConsentDetailsConverter {

    private static volatile InternationalPaymentConsentDetailsConverter instance;
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");

    /*
     * Double checked locking principle to ensure that only one instance 'DomesticPaymentConsentDetailsConverter' is created
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

        details.setMerchantName(isNotNull(consentDetails.get("oauth2ClientName")) ?
                consentDetails.get("oauth2ClientName").getAsString() :
                null);

        if (!isNotNull(consentDetails.get("data"))) {
            details.setPaymentReference(null);
            details.setCurrencyOfTransfer(null);
            details.setExchangeRateInformation(null);
            details.setInstructedAmount(null);
        } else {
            JsonObject data = consentDetails.getAsJsonObject("data");
            details.setExchangeRateInformation(isNotNull(data.get("ExchangeRateInformation")) ?
                    data.getAsJsonObject("ExchangeRateInformation") :
                    null);

            if (isNotNull(data.get("Initiation"))) {
                JsonObject initiation = data.getAsJsonObject("Initiation");

                details.setPaymentReference(isNotNull(initiation.get("RemittanceInformation")) && isNotNull(initiation.getAsJsonObject("RemittanceInformation").get("Reference")) ?
                        initiation.getAsJsonObject("RemittanceInformation").get("Reference").getAsString() : null);

                details.setCurrencyOfTransfer(isNotNull(initiation.get("CurrencyOfTransfer")) ?
                        initiation.get("CurrencyOfTransfer").getAsString() : null);

                details.setInstructedAmount(isNotNull(initiation.get("InstructedAmount")) ?
                        initiation.getAsJsonObject("InstructedAmount") :
                        null);

            }
        }
        return details;
    }

    public final InternationalPaymentConsentDetails toInternationalPaymentConsentDetails(JsonObject consentDetails) {
        return mapping(consentDetails);
    }
}
