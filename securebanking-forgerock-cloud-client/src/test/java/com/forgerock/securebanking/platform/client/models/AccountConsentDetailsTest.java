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
package com.forgerock.securebanking.platform.client.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.gson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Unit Test for Account Consent Details
 */
@Slf4j
public class AccountConsentDetailsTest {

    private static final String CONSENT_ID = "AAC_886511e2-78f0-4a14-9ab8-221360815aac";
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
        JsonObject consentDetails = aValidAccountConsentDetails(CONSENT_ID, CLIENT_ID);

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
                "\"type\" : \"AccountConsentDetails\"," +
                "\"id\" : \"" + consentId + "\"," +
                "\"data\" : {" +
                "\"Permissions\" : [ \"ReadAccountsDetail\", \"ReadBalances\", \"ReadTransactionsDetail\" ]," +
                "\"ExpirationDateTime\" : \"2021-10-02T14:31:14.923+01:00\"," +
                "\"TransactionFromDateTime\" : \"2021-09-30T14:31:14.935+01:00\"," +
                "\"TransactionToDateTime\" : \"2021-10-01T14:31:14.935+01:00\"," +
                "\"ConsentId\" : \"" + consentId + "\"," +
                "\"Status\" : \"AwaitingAuthorisation\"," +
                "\"CreationDateTime\" : \"2021-10-01T14:31:14.935+01:00\"," +
                "\"StatusUpdateDateTime\" : \"2021-10-01T14:31:14.935+01:00\"}," +
                "\"accountIds\" : [ \"8f10f873-2b32-4306-aeea-d11004f92200\" ]," +
                "\"oauth2ClientId\" : \"" + clientId + "\"," +
                "\"oauth2ClientName\" : \"AISP Name\"" +
                "}";
    }

}
