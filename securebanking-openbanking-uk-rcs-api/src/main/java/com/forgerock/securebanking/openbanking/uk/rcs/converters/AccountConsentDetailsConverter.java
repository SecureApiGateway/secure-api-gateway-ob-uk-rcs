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

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Instant;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.OAUTH2_CLIENT_NAME;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Converter class to map {@link JsonObject} to {@link AccountsConsentDetails}
 */
@Slf4j
@NoArgsConstructor
public class AccountConsentDetailsConverter {

    private static volatile AccountConsentDetailsConverter instance;

    /*
     * Double check locking principle to ensure that only one instance 'AccountConsentDetailsConverter' is created
     */
    public static AccountConsentDetailsConverter getInstance() {
        if (instance == null) {
            synchronized (AccountConsentDetailsConverter.class) {
                if (instance == null) {
                    instance = new AccountConsentDetailsConverter();
                }
            }
        }
        return instance;
    }

    public AccountsConsentDetails mapping(JsonObject consentDetails) {
        AccountsConsentDetails details = new AccountsConsentDetails();

        if (!consentDetails.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = consentDetails.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (!isNotNull(consentDataElement)) {
                details.setFromTransaction(null);
                details.setToTransaction(null);
                details.setExpiredDate(null);
                details.setPermissions(null);
            } else {
                JsonObject data = consentDataElement.getAsJsonObject();

                details.setFromTransaction(isNotNull(data.get(TRANSACTION_FROM_DATETIME)) ?
                        Instant.parse(data.get(TRANSACTION_FROM_DATETIME).getAsString()).toDateTime() :
                        null);

                details.setToTransaction(isNotNull(data.get(TRANSACTION_TO_DATETIME)) ?
                        Instant.parse(data.get(TRANSACTION_TO_DATETIME).getAsString()).toDateTime() :
                        null);

                details.setExpiredDate(isNotNull(data.get(EXPIRATION_DATETIME)) ?
                        Instant.parse(data.get(EXPIRATION_DATETIME).getAsString()).toDateTime() :
                        null);

                details.setPermissions(isNotNull(data.get(PERMISSIONS)) ?
                        data.getAsJsonArray(PERMISSIONS) :
                        null);
            }
        }
        return details;
    }

    public final AccountsConsentDetails toAccountConsentDetails(JsonObject consentDetails) {
        return mapping(consentDetails);
    }
}
