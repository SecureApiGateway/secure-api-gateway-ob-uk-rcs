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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAccountIdentifier;
import com.forgerock.securebanking.platform.client.Constants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Unit Test for {@link ConsentClientDecisionRequest}
 */
@Slf4j
public class ConsentDecisionTest {

    private static final String USER_ID = "7e47a733-005b-4031-8622-18064ac373b7";
    private static final String ACCOUNT_ID = "8f10f873-2b32-4306-aeea-d11004f92200";
    private static final String DEB_ACC_IDENTIFIER = "79126738233670";
    private ObjectMapper mapper;

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
    public void shouldDeserializeAccountsConsentDecision() throws JsonProcessingException {
        // Given
        String json = getJsonAccounts();

        // When
        ConsentClientDecisionRequest consentDecision = mapper.readValue(json, ConsentClientDecisionRequest.class);

        // Then
        assertThat(consentDecision).isNotNull();
        assertThat(consentDecision.getAccountIds().isEmpty()).isEqualTo(false);
        assertThat(consentDecision.getAccountIds().get(0)).contains(ACCOUNT_ID);
    }

    @Test
    public void shouldDeserializePaymentsConsentDecision() throws JsonProcessingException {
        // Given
        String json = getJsonPayments();

        // When
        ConsentClientDecisionRequest consentDecision = mapper.readValue(json, ConsentClientDecisionRequest.class);

        // Then
        assertThat(consentDecision).isNotNull();
        assertThat(consentDecision.getAccountIds()).isNull();
        assertThat(consentDecision.getData().getDebtorAccount()).isNotNull();
        assertThat(consentDecision.getData().getDebtorAccount().getIdentification()).isEqualTo(DEB_ACC_IDENTIFIER);

    }

    @Test
    public void shouldSerializeAccounts() throws JsonProcessingException {
        // Given

        ConsentClientDecisionRequest consentDecision = ConsentClientDecisionRequest.builder()
                .data(
                        ConsentClientDecisionRequestData.builder()
                                .status(Constants.ConsentDecisionStatus.AUTHORISED)
                                .build()
                ).accountIds(List.of("12345", "67890"))
                .resourceOwnerUsername(USER_ID)
                .build();

        // When
        String json = mapper.writeValueAsString(consentDecision);
        log.info("Json Serialize as String \n{}", json);

        // Then
        assertThat(json).containsPattern("\"resourceOwnerUsername\".:.\"" + USER_ID + "\"");
        assertThat(json).containsPattern("\"Status\".:.\"" + Constants.ConsentDecisionStatus.AUTHORISED + "\"");
    }

    @Test
    public void shouldSerializePayments() throws JsonProcessingException {
        // Given

        ConsentClientDecisionRequest consentDecision = ConsentClientDecisionRequest.builder()
                .data(
                        ConsentClientDecisionRequestData.builder()
                                .status(Constants.ConsentDecisionStatus.AUTHORISED)
                                .debtorAccount(FRAccountIdentifier.builder()
                                        .identification(DEB_ACC_IDENTIFIER)
                                        .name("name")
                                        .schemeName("schemeName")
                                        .secondaryIdentification("secId")
                                        .build()
                                )
                                .build()
                )
                .resourceOwnerUsername(USER_ID)
                .build();

        // When
        String json = mapper.writeValueAsString(consentDecision);
        log.info("Json Serialize as String \n{}", json);

        // Then
        assertThat(json).containsPattern("\"resourceOwnerUsername\".:.\"" + USER_ID + "\"");
        assertThat(json).containsPattern("\"Status\".:.\"" + Constants.ConsentDecisionStatus.AUTHORISED + "\"");
        assertThat(json).containsPattern("\"identification\".:.\"" + DEB_ACC_IDENTIFIER + "\"");
    }

    private String getJsonAccounts() {
        return getJsonAccounts(UUID.randomUUID().toString());
    }

    private String getJsonPayments() {
        return getJsonPayments(UUID.randomUUID().toString());
    }

    private String getJsonAccounts(String userId) {
        return "{" +
                "\"type\" : \"ConsentClientDecisionRequest\"," +
                "\"data\" : {" +
                "\"Status\" : \"" + Constants.ConsentDecisionStatus.AUTHORISED + "\"" +
                "}," +
                "\"accountIds\" : [ \"" + ACCOUNT_ID + "\" ]," +
                "\"resourceOwnerUsername\" : \"" + userId + "\"" +
                "}";
    }

    private String getJsonPayments(String userId) {
        return "{" +
                "\"type\" : \"ConsentClientDecisionRequest\"," +
                "\"data\" : {" +
                "\"Status\" : \"" + Constants.ConsentDecisionStatus.AUTHORISED + "\"," +
                "\"debtorAccount\": {" +
                "\"schemeName\": \"UK.OBIE.SortCodeAccountNumber\"," +
                "\"identification\": \"" + DEB_ACC_IDENTIFIER + "\"," +
                "\"name\": \"7b78b560-6057-41c5-bf1f-1ed590b1c30b\"," +
                "\"secondaryIdentification\": \"49704112\" }" +
                "}," +
                "\"resourceOwnerUsername\" : \"" + userId + "\"" +
                "}";
    }

}
