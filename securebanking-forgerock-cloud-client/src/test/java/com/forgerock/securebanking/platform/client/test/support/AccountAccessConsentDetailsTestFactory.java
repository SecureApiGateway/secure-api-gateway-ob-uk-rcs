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
package com.forgerock.securebanking.platform.client.test.support;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.securebanking.platform.client.ConsentStatusCode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Test data factory for Account Consent Details
 */
public class AccountAccessConsentDetailsTestFactory {

    public static final Gson gson = new Gson();

    public static JsonObject aValidAccountConsentDetails() {
        return aValidAccountConsentDetailsBuilder(randomUUID().toString());
    }

    public static JsonObject aValidAccountConsentDetails(String consentId) {
        return aValidAccountConsentDetailsBuilder(consentId);
    }

    public static JsonObject aValidAccountConsentDetails(String consentId, String clientId) {
        return aValidAccountConsentDetailsBuilder(consentId, clientId);
    }

    public static JsonObject aValidAccountConsentDetailsBuilder(String consentId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        consent.add("data", aValidAccountConsentDataDetailsBuilder(consentId));
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "AISP Name");
        consent.addProperty("accountIds", gson.toJson(List.of(UUID.randomUUID().toString())));

        return consent;
    }

    public static JsonObject aValidAccountConsentDetailsBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        consent.add("data", aValidAccountConsentDataDetailsBuilder(consentId));
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "AISP Name");
        consent.addProperty("accountIds", gson.toJson(List.of(UUID.randomUUID().toString())));

        return consent;
    }

    public static JsonObject aValidAccountConsentDataDetailsBuilderOnlyMandatoryFields(String consentId) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", consentId);
        data.add("Permissions", gson.toJsonTree(List.of(
                FRExternalPermissionsCode.READACCOUNTSDETAIL.getValue(),
                FRExternalPermissionsCode.READBALANCES.getValue(),
                FRExternalPermissionsCode.READTRANSACTIONSDETAIL.getValue()
        )));
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", ConsentStatusCode.AWAITINGAUTHORISATION.toString());
        return data;
    }

    public static JsonObject aValidAccountConsentDataDetailsBuilder(String consentId) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", consentId);
        data.add("Permissions", gson.toJsonTree(List.of(
                FRExternalPermissionsCode.READACCOUNTSDETAIL.getValue(),
                FRExternalPermissionsCode.READBALANCES.getValue(),
                FRExternalPermissionsCode.READTRANSACTIONSDETAIL.getValue()
        )));
        data.addProperty("ExpirationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).plusDays(1).toString());
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("StatusUpdateDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("TransactionFromDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).minusDays(1).toString());
        data.addProperty("TransactionToDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", ConsentStatusCode.AWAITINGAUTHORISATION.toString());
        return data;
    }
}
