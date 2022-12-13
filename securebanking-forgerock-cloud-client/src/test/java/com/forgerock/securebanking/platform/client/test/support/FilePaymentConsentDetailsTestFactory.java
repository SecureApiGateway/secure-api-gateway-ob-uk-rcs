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
package com.forgerock.securebanking.platform.client.test.support;

import com.forgerock.securebanking.platform.client.ConsentStatusCode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Test data factory for File Payment Consent Details
 */
public class FilePaymentConsentDetailsTestFactory {

    public static final Gson gson = new Gson();

    public static JsonObject aValidFilePaymentConsentDetails() {
        return aValidFilePaymentConsentDetailsBuilder(randomUUID().toString());
    }

    public static JsonObject aValidFilePaymentConsentDetails(String consentId) {
        return aValidFilePaymentConsentDetailsBuilder(consentId);
    }

    public static JsonObject aValidFilePaymentConsentDetails(String consentId, String clientId) {
        return aValidFilePaymentConsentDetailsBuilder(consentId, clientId);
    }

    public static JsonObject aValidFilePaymentConsentDetailsBuilder(String consentId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        final JsonObject obIntent = new JsonObject();
        obIntent.add("Data", aValidFilePaymentConsentDataDetailsBuilder(consentId));
        consent.add("OBIntentObject", obIntent);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        return consent;
    }

    public static JsonObject aValidFilePaymentConsentDetailsBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        final JsonObject obIntent = new JsonObject();
        obIntent.add("Data", aValidFilePaymentConsentDataDetailsBuilder(consentId));
        consent.add("OBIntentObject", obIntent);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        return consent;
    }

    public static JsonObject aValidFilePaymentConsentDataDetailsBuilder(String consentId) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", consentId);
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("StatusUpdateDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", ConsentStatusCode.AWAITINGAUTHORISATION.toString());
        data.add("Initiation", aValidFRWriteDomesticDataInitiationBuilder());
        data.add("Charges", aValidChargesBuilder());
        return data;
    }

    public static JsonObject aValidFRWriteDomesticDataInitiationBuilder() {
        JsonObject data = new JsonObject();
        data.addProperty("FileType", "UK.OBIE.pain.001.001.08");
        data.addProperty("FileHash", "VEzqICCQvK7NHt8g75Kfbsb0XW5Wmeg3pgAgpN6oLQ8=");
        data.addProperty("FileReference", "XmlExample");
        data.addProperty("NumberOfTransactions", "3");
        data.addProperty("ControlSum", "11500000");
        data.addProperty("RequestedExecutionDateTime", "2023-09-27T13:03:06+00:00");
        data.addProperty("LocalInstrument", "null");

        data.add("DebtorAccount", null);
        data.add("RemittanceInformation", JsonParser.parseString("{\n" +
                "   \"Unstructured\":\"Internal ops code 5120101\",\n" +
                "   \"Reference\":\"FRESCO-101\"\n" +
                "}"));

        data.add("SupplementaryData", null);
        return data;
    }

    public static JsonArray aValidChargesBuilder() {
        JsonArray charges = new JsonArray();
        charges.add(JsonParser.parseString("{\n" +
                "        \"ChargeBearer\": \"BorneByDebtor\",\n" +
                "        \"Type\": \"UK.OBIE.CHAPSOut\",\n" +
                "        \"Amount\": { \"Amount\": '12.91', \"Currency\": 'GBP' }" +
                "      }"));
        charges.add(JsonParser.parseString("{\n" +
                "        \"ChargeBearer\": \"BorneByDebtor\",\n" +
                "        \"Type\": \"UK.OBIE.CHAPSOut\",\n" +
                "        \"Amount\": { \"Amount\": '8.2', \"Currency\": 'USD' }" +
                "      }"));
        return charges;
    }
}
