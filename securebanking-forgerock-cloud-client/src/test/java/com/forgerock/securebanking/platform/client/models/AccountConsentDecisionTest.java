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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.forgerock.securebanking.platform.client.Constants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Unit Test for {@link AccountConsentDecision}
 */
@Slf4j
public class AccountConsentDecisionTest {

    private ObjectMapper mapper;
    private static final String USER_ID = "7e47a733-005b-4031-8622-18064ac373b7";

    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JodaModule());
        mapper.setTimeZone(TimeZone.getDefault());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void shouldDeserialize() throws JsonProcessingException {
        // Given
        String json = getJson();

        // When
        AccountConsentDecision accountConsentDecision = mapper.readValue(json, AccountConsentDecision.class);

        // Then
        assertThat(accountConsentDecision).isNotNull();
    }

    @Test
    public void shouldSerialize() throws JsonProcessingException {
        // Given

        AccountConsentDecision accountConsentDecision = AccountConsentDecision.builder()
                .data(
                        ConsentDecisionData.builder()
                                .status(Constants.ConsentDecision.APPROVED)
                                .build()
                ).accountIds(List.of("12345", "67890"))
                .resourceOwnerUsername(USER_ID)
                .build();

        // When
        String json = mapper.writeValueAsString(accountConsentDecision);
        log.info("Json Serialize as String \n{}", json);

        // Then
        assertThat(json).containsPattern("\"resourceOwnerUsername\".:.\"" + USER_ID + "\"");
        assertThat(json).containsPattern("\"Status\".:.\"" + Constants.ConsentDecision.APPROVED + "\"");
    }

    private String getJson() {
        return getJson(UUID.randomUUID().toString());
    }

    private String getJson(String userId) {
        return "{" +
                "\"type\" : \"AccountConsentDecision\"," +
                "\"data\" : {" +
                "\"Status\" : \"" + Constants.ConsentDecision.APPROVED + "\"" +
                "}," +
                "\"accountIds\" : [ \"8f10f873-2b32-4306-aeea-d11004f92200\" ]," +
                "\"resourceOwnerUsername\" : \"" + userId + "\"" +
                "}";
    }

}