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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Test data factory for Domestic Payment Consent Details
 */
public class InternationalScheduledPaymentConsentDetailsTestFactory {

    public static final Gson gson = new Gson();

    public static JsonObject aValidInternationalScheduledPaymentConsentDetails() {
        return aValidInternationalScheduledPaymentConsentDetailsBuilder(randomUUID().toString());
    }

    public static JsonObject aValidInternationalScheduledPaymentConsentDetails(String consentId) {
        return aValidInternationalScheduledPaymentConsentDetailsBuilder(consentId);
    }

    public static JsonObject aValidInternationalScheduledPaymentConsentDetails(String consentId, String clientId) {
        return aValidInternationalScheduledPaymentConsentDetailsBuilder(consentId, clientId);
    }

    public static JsonObject aValidInternationalScheduledPaymentConsentDetailsBuilder(String consentId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        consent.add("data", aValidInternationalScheduledPaymentConsentDataDetailsBuilder(consentId));
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountIds", gson.toJson(List.of(UUID.randomUUID().toString())));

        return consent;
    }

    public static JsonObject aValidInternationalScheduledPaymentConsentDetailsBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        consent.add("data", aValidInternationalScheduledPaymentConsentDataDetailsBuilder(consentId));
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountIds", gson.toJson(List.of(UUID.randomUUID().toString())));

        return consent;
    }

    public static JsonObject aValidInternationalScheduledPaymentConsentDataDetailsBuilder(String consentId) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", consentId);
        data.addProperty("Permission", "Create");
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("StatusUpdateDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", ConsentStatusCode.AWAITINGAUTHORISATION.toString());
        data.add("Initiation", aValidFRWriteDomesticDataInitiationBuilder());
        return data;
    }

    public static JsonObject aValidFRWriteDomesticDataInitiationBuilder() {
        JsonObject data = new JsonObject();
        data.addProperty("InstructionIdentification", "ACME412");
        data.addProperty("EndToEndIdentification", "FRESCO.21302.GFX.20");
        data.addProperty("LocalInstrument", "UK.OBIE.BACS");
        data.addProperty("InstructionPriority", "Normal");
        data.addProperty("Purpose", "CDCD");
        data.addProperty("ExtendedPurpose", "Extended Purpose");
        data.addProperty("CurrencyOfTransfer", "USD");
        data.addProperty("DestinationCountryCode", "GB");
        data.addProperty("RequestedExecutionDateTime", "2022-05-30T15:15:13+00:00");
        data.add("InstructedAmount", JsonParser.parseString("{ \"Amount\": '89.91', \"Currency\": 'GBP' }"));
        data.add("DebtorAccount", null);
        data.add("CreditorAccount", JsonParser.parseString("{\n" +
                "        \"SchemeName\": \"UK.OBIE.SortCodeAccountNumber\",\n" +
                "        \"Identification\": \"08080021325698\",\n" +
                "        \"Name\": \"ACME Inc\",\n" +
                "        \"SecondaryIdentification\": \"0002\"\n" +
                "      }"));
        data.add("ExchangeRateInformation", JsonParser.parseString("{\n" +
                "        \"UnitCurrency\": \"EUR\",\n" +
                "        \"ExchangeRate\": \"10\",\n" +
                "        \"RateType\": \"Agreed\",\n" +
                "        \"ExpirationDateTime\": \"2022-05-30T15:15:13+00:00\",\n" +
                "        \"ContractIdentification\": \"T102993\"\n" +
                "      }"));
        data.add("RemittanceInformation", JsonParser.parseString("{\n" +
                "        \"Reference\": \"FRESCO-101\",\n" +
                "        \"Unstructured\": \"Internal ops code 5120101\"\n" +
                "      }"));
        data.add("SupplementaryData", null);
        return data;
    }

}
