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
package com.forgerock.securebanking.platform.client.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.forgerock.securebanking.platform.client.test.support.DomesticPaymentConsentDetailsTestFactory.aValidDomesticPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.DomesticPaymentConsentDetailsTestFactory.gson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Unit Test for Domestic Payment Consent Details
 */
@Slf4j
public class DomesticPaymentConsentDetailsTest {

    private static final String CONSENT_ID = "PDC_886511e2-78f0-4a14-9ab8-221360815aac";
    private static final String CLIENT_ID = "7e47a733-005b-4031-8622-18064ac373b7";

    @Test
    public void shouldDeserialize() throws JsonProcessingException {
        // Given
        String json = getJson();

        // When
        JsonObject consentDetails = new JsonParser().parse(json).getAsJsonObject();

        // Then
        assertThat(consentDetails).isNotNull();
    }

    @Test
    public void shouldSerialize() throws JsonProcessingException {
        // Given
        JsonObject consentDetails = aValidDomesticPaymentConsentDetails(CONSENT_ID, CLIENT_ID);

        // When
        String json = gson.toJson(consentDetails);
        log.info("Json Serialize as String \n{}", json);

        // Then
        assertThat(json).containsPattern("\"ConsentId\":\"" + CONSENT_ID + "\"");
        assertThat(json).containsPattern("\"oauth2ClientId\":\"" + CLIENT_ID + "\"");
    }

    private String getJson() {
        return getJson(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    private String getJson(String consentId, String clientId) {
        return "{" +
                "\"type\" : \"DomesticPaymentConsentDetails\"," +
                "\"id\" : \"" + consentId + "\"," +
                "\"data\" : {" +
                "\"Initiation\" : {" +
                "\"InstructionIdentification\" : \"ACME412\"," +
                "\"EndToEndIdentification\" : \"FRESCO.21302.GFX.20\"," +
                "\"InstructedAmount\" : {" +
                "\"Amount\" : \"165.88\"," +
                "\"Currency\" : \"GBP\"}," +
                "\"CreditorAccount\" : {" +
                "\"SchemeName\" : \"UK.OBIE.SortCodeAccountNumber\"," +
                "\"Identification\" : \"08080021325698\"," +
                "\"Name\" : \"ACME Inc\"," +
                "\"SecondaryIdentification\" : \"0002\"}," +
                "\"CreditorAccountRemittanceInformation\" : {" +
                "\"Reference\" : \"FRESCO-101\"," +
                "\"Unstructured\" : \"Internal ops code 5120101\"}}," +
                "\"ConsentId\" : \"" + consentId + "\"," +
                "\"Status\" : \"AwaitingAuthorisation\"," +
                "\"CreationDateTime\" : \"2021-10-01T14:31:14.935+01:00\"," +
                "\"StatusUpdateDateTime\" : \"2021-10-01T14:31:14.935+01:00\"}," +
                "\"accountIds\" : [ \"8f10f873-2b32-4306-aeea-d11004f92200\" ]," +
                "\"oauth2ClientId\" : \"" + clientId + "\"," +
                "\"oauth2ClientName\" : \"PISP Name\"" +
                "}";
    }

}
