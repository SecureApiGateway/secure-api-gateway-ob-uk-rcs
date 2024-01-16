/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ConsentDecisionDeserializer}
 */
public class ConsentDecisionDeserializerTest {

    private ObjectMapper mapper;
    private String ACC_ID = "8614e6bf-2ba3-40eb-9fd4-5a4d77785f50";

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
    public void shouldDeserializeAccountConsentDecision() throws OBErrorException {

        ConsentDecisionDeserialized result = ConsentDecisionDeserializer.deserializeConsentDecision(
                aValidAccountConsentDecisionSerialised(),
                mapper,
                ConsentDecisionDeserialized.class);

        assertThat(result.getAccountIds()).isNotNull();
        assertThat(result.getAccountIds().size()).isEqualTo(3);
    }

    @Test
    public void shouldDeserializePaymentConsentDecision() throws OBErrorException {

        ConsentDecisionDeserialized result = ConsentDecisionDeserializer.deserializeConsentDecision(
                aValidPaymentConsentDecisionSerialised(),
                mapper,
                ConsentDecisionDeserialized.class);

        assertThat(result.getAccountIds()).isNull();
        assertThat(result.getDebtorAccount()).isNotNull();
        assertThat(result.getDebtorAccount().getAccountId()).isEqualTo(ACC_ID);
    }

    private String aValidAccountConsentDecisionSerialised() {
        return "{\"consentJwt\":\"afibelaDFDFCaoehfldXXX\"," +
                "\"decision\":\"Authorised\"," +
                "\"accountIds\":[\"ddb08e74-e22a-4012-99f3-154ba52eb0eb\"," +
                "\"1dfa82b8-7f95-4d6a-a29f-de3244b2bafd\",\"73b579ec-6eca-4212-a972-602d30d62b5c\"]}";
    }

    private String aValidPaymentConsentDecisionSerialised() {
        return "{\"consentJwt\":\"eyJ0eXAiOiJKV1QiLCJraWQiO\"," +
                "\"decision\":\"Authorised\"," +
                "\"debtorAccount\":{" +
                "\"accountId\":\"" +ACC_ID+"\"," +
                "\"status\":\"Enabled\"," +
                "\"statusUpdateDateTime\":\"2023-01-11T07:00:05.000Z\"," +
                "\"currency\":\"GBP\"," +
                "\"accountType\":\"Personal\"," +
                "\"accountSubType\":\"CurrentAccount\"," +
                "\"nickname\":\"UK Bills\"," +
                "\"openingDate\":\"2023-01-10T07:00:05.000Z\"," +
                "\"maturityDate\":\"2023-01-12T07:00:05.000Z\"," +
                "\"accounts\":[{" +
                "\"schemeName\":\"UK.OBIE.SortCodeAccountNumber\"," +
                "\"identification\":\"3898807675771\"," +
                "\"name\":\"3ee885ee-ae0b-45a5-b061-8c19fdaab76f\"," +
                "\"secondaryIdentification\":\"16282771\"" +
                "}]" +
                "},\"latestStatementId\":\"f9ad6fe2-7a2e-4f44-ad58-d87c4f6e110c\"," +
                "\"created\":\"2023-01-11T07:00:05.000Z\"," +
                "\"balances\":[{" +
                "\"accountId\":\"8614e6bf-2ba3-40eb-9fd4-5a4d77785f50\"," +
                "\"creditDebitIndicator\":\"Debit\"," +
                "\"type\":\"InterimAvailable\"," +
                "\"dateTime\":\"2023-01-11T07:00:06.000Z\"," +
                "\"amount\":{" +
                "\"amount\":\"7853.64\"," +
                "\"currency\":\"GBP\"" +
                "}}]}";
    }
}
