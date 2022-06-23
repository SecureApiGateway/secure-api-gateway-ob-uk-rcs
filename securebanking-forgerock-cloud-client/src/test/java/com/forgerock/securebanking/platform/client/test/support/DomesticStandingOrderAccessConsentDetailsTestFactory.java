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

import com.forgerock.securebanking.platform.client.ConsentStatusCode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Test data factory for Domestic Payment Consent Details
 */
public class DomesticStandingOrderAccessConsentDetailsTestFactory {

    public static final Gson gson = new Gson();
    public static final Random random = new Random();
    public static final JsonParser parser = new JsonParser();

    public static JsonObject aValidDomesticStandingOrderConsentDetails() {
        return aValidDomesticStandingOrderConsentDetailsBuilder(randomUUID().toString());
    }

    public static JsonObject aValidDomesticStandingOrderConsentDetails(String consentId) {
        return aValidDomesticStandingOrderConsentDetailsBuilder(consentId);
    }

    public static JsonObject aValidDomesticStandingOrderConsentDetails(String consentId, String clientId) {
        return aValidDomesticStandingOrderConsentDetailsBuilder(consentId, clientId);
    }

    public static JsonObject aValidDomesticStandingOrderConsentDetailsBuilder(String consentId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        consent.add("data", aValidDomesticStandingOrderConsentDataDetailsBuilder(consentId));
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountIds", gson.toJson(List.of(UUID.randomUUID().toString())));

        return consent;
    }

    public static JsonObject aValidDomesticStandingOrderConsentDetailsBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        consent.add("data", aValidDomesticStandingOrderConsentDataDetailsBuilder(consentId));
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountIds", gson.toJson(List.of(UUID.randomUUID().toString())));

        return consent;
    }

    public static JsonObject aValidDomesticStandingOrderConsentDataDetailsBuilder(String consentId) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", consentId);
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("StatusUpdateDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", ConsentStatusCode.AWAITINGAUTHORISATION.toString());
        data.add("Initiation", aValidFRWriteDomesticDataInitiationBuilder());
        return data;
    }

    public static JsonObject aValidFRWriteDomesticDataInitiationBuilder() {
        JsonObject data = new JsonObject();
        data.add("DebtorAccount", null);
        data.add("CreditorAccount", parser.parse("{\n" +
                "        \"SchemeName\": \"UK.OBIE.SortCodeAccountNumber\",\n" +
                "        \"Identification\": \"08080021325698\",\n" +
                "        \"Name\": \"ACME Inc\",\n" +
                "        \"SecondaryIdentification\": \"0002\"\n" +
                "      }"));
        data.addProperty("Reference", "FRESCO-101");
        data.add("SupplementaryData", null);
        data.add("FinalPaymentAmount", parser.parse("{ \"Amount\": '319.91', \"Currency\": 'GBP' }"));
        data.add("FirstPaymentAmount", parser.parse("{ \"Amount\": '119.91', \"Currency\": 'GBP' }"));
        data.add("RecurringPaymentAmount", parser.parse("{ \"Amount\": '813.91', \"Currency\": 'GBP' }"));
        data.addProperty("FinalPaymentDateTime", "2022-09-30T15:15:13+00:00");
        data.addProperty("FirstPaymentDateTime", "2022-05-30T15:15:13+00:00");
        data.addProperty("RecurringPaymentDateTime", "2022-06-30T15:15:13+00:00");
        data.addProperty("Frequency", "EvryDay");
        return data;
    }

}
