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
package com.forgerock.securebanking.openbanking.uk.rcs.converters.accounts;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Converter class to map {@link JsonObject} to {@link AccountsConsentDetails}
 */
@Slf4j
@NoArgsConstructor
public class AccountConsentDetailsConverter {

    private static volatile AccountConsentDetailsConverter instance;

    /*
     * Double checked locking principle to ensure that only one instance 'AccountConsentDetailsConverter' is created
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
        details.setAispName(consentDetails.get("oauth2ClientName") != null ?
                consentDetails.get("oauth2ClientName").getAsString() :
                null);
        details.setFromTransaction(consentDetails.getAsJsonObject("data") != null &&
                consentDetails.getAsJsonObject("data").get("TransactionFromDateTime") != null ?
                consentDetails.getAsJsonObject("data").get("TransactionFromDateTime").getAsString() :
                null);
        details.setToTransaction(consentDetails.getAsJsonObject("data") != null &&
                consentDetails.getAsJsonObject("data").get("TransactionToDateTime") != null ?
                consentDetails.getAsJsonObject("data").get("TransactionToDateTime").getAsString() :
                null);
        details.setExpiredDate(consentDetails.getAsJsonObject("data") != null &&
                consentDetails.getAsJsonObject("data").get("ExpirationDateTime") != null ?
                consentDetails.getAsJsonObject("data").get("ExpirationDateTime").getAsString() :
                null);
        details.setPermissions(consentDetails.get("data") != null ?
                consentDetails.getAsJsonObject("data").getAsJsonArray("Permissions") :
                null);
        return details;
    }

    public final AccountsConsentDetails toAccountConsentDetails(JsonObject consentDetails) {
        return mapping(consentDetails);
    }
}
