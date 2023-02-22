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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support;

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.ConsentStatusCode;
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
 * Test data factory for International Standing Order Consent Details
 */
public class InternationalStandingOrderConsentDetailsTestFactory {

    public static final Gson gson = new Gson();

    public static JsonObject aValidInternationalStandingOrderConsentDetails() {
        return aValidInternationalStandingOrderConsentDetailsBuilder(randomUUID().toString());
    }

    public static JsonObject aValidInternationalStandingOrderConsentDetails(String consentId) {
        return aValidInternationalStandingOrderConsentDetailsBuilder(consentId);
    }

    public static JsonObject aValidInternationalStandingOrderConsentDetails(String consentId, String clientId) {
        return aValidInternationalStandingOrderConsentDetailsBuilder(consentId, clientId);
    }

    public static JsonObject aValidInternationalStandingOrderConsentDetailsBuilder(String consentId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        final JsonObject obIntent = new JsonObject();
        obIntent.add("Data", aValidInternationalStandingOrderConsentDataDetailsBuilder(consentId));
        consent.add("OBIntentObject", obIntent);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        return consent;
    }

    public static JsonObject aValidInternationalStandingOrderConsentDetailsBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        final JsonObject obIntent = new JsonObject();
        obIntent.add("Data", aValidInternationalStandingOrderConsentDataDetailsBuilder(consentId));
        consent.add("OBIntentObject", obIntent);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        return consent;
    }

    public static JsonObject aValidInternationalStandingOrderConsentDataDetailsBuilder(String consentId) {
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
        data.addProperty("Frequency", "EvryWorkgDay");
        data.addProperty("Reference", "Ipsum Non Arcu Inc.");
        data.addProperty("NumberOfPayments", "1");
        data.addProperty("FirstPaymentDateTime", "2022-09-27T13:03:06+00:00");
        data.addProperty("FinalPaymentDateTime", "2022-09-27T13:03:06+00:00");
        data.addProperty("Purpose", "CDCD");
        data.addProperty("ExtendedPurpose", "Extended purpose");
        data.addProperty("CurrencyOfTransfer", "USD");
        data.addProperty("DestinationCountryCode", "GB");

        data.add("InstructedAmount", JsonParser.parseString("{\"Amount\":\"10.01\",\"Currency\":\"GBP\"}"));
        data.add("DebtorAccount", null);
        data.add("CreditorAccount", JsonParser.parseString("{\n" +
                "   \"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\n" +
                "   \"Identification\":\"90611424625555\",\n" +
                "   \"Name\":\"Mr Steven Morrissey\",\n" +
                "   \"SecondaryIdentification\":\"44\"\n" +
                "}"));
        data.add("Creditor", null);
        data.add("CreditorAgent", null);

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
