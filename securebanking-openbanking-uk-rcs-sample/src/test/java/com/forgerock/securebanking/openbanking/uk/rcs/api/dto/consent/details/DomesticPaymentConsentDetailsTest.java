/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.FRAmountTestDataFactory.aValidFRAmount;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.joda.time.DateTime.now;

/**
 * Unit test for {@link DomesticPaymentConsentDetails}.
 */
public class DomesticPaymentConsentDetailsTest {

    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ"));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void shouldDeserialize() throws JsonProcessingException {
        // Given
        String json = getJson();

        // When
        DomesticPaymentConsentDetails consentDetails = mapper.readValue(json, DomesticPaymentConsentDetails.class);

        // Then
        assertThat(consentDetails).isNotNull();
    }

    @Test
    public void shouldSerialize() throws JsonProcessingException {
        // Given
        DomesticPaymentConsentDetails consentDetails = DomesticPaymentConsentDetails.builder()
                .instructedAmount(aValidFRAmount())
                .accounts(List.of(aValidFRAccountWithBalance()))
                .username("aValidUsername")
                .logo("http://www.logo.com")
                .clientId("12345")
                .merchantName("Test Merchant")
                .pispName("Test PISP")
                .paymentReference("A Payment reference")
                .build();
        FRAccountWithBalance accountWithBalance = consentDetails.getAccounts().get(0);
        String expectedJson = getJson(
                accountWithBalance.getAccount().getStatusUpdateDateTime(),
                accountWithBalance.getAccount().getOpeningDate(),
                accountWithBalance.getBalances().get(0).getDateTime());

        // When
        String json = mapper.writeValueAsString(consentDetails);

        // Then
        assertThat(json).isEqualTo(expectedJson);
    }

    private String getJson() {
        return getJson(now(), now(), now());
    }

    private String getJson(DateTime statusUpdateDateTime,
                           DateTime openingDate,
                           DateTime dateTime) {
        return "{" +
                "\"type\":\"DomesticPaymentConsentDetails\"," +
                "\"instructedAmount\":{" +
                "\"amount\":\"10.00\"," +
                "\"currency\":\"GBP\"" +
                "}," +
                "\"accounts\":[" +
                "{" +
                "\"id\":\"123456\"," +
                "\"account\":{" +
                "\"accountId\":\"1234\"," +
                "\"status\":\"ENABLED\"," +
                "\"statusUpdateDateTime\":\"" + statusUpdateDateTime + "\"," +
                "\"currency\":\"GBP\"," +
                "\"accountType\":\"PERSONAL\"," +
                "\"accountSubType\":\"CURRENTACCOUNT\"," +
                "\"description\":\"A personal current account\"," +
                "\"nickname\":\"House Account\"," +
                "\"openingDate\":\"" + openingDate + "\"," +
                "\"accounts\":[" +
                "{" +
                "\"schemeName\":\"UK.OBIE.SortCodeAccountNumber\"," +
                "\"identification\":\"40400411290112\"," +
                "\"name\":\"Mr A Jones\"" +
                "}" +
                "]," +
                "\"servicer\":{" +
                "\"schemeName\":\"UK.OBIE.SortCodeAccountNumber\"," +
                "\"identification\":\"9876\"" +
                "}" +
                "}," +
                "\"balances\":[" +
                "{" +
                "\"accountId\":\"12345\"," +
                "\"creditDebitIndicator\":\"CREDIT\"," +
                "\"type\":\"INTERIMAVAILABLE\"," +
                "\"dateTime\":\""+ dateTime + "\"," +
                "\"amount\":{" +
                "\"amount\":\"10.00\"," +
                "\"currency\":\"GBP\"" +
                "}" +
                "}" +
                "]" +
                "}" +
                "]," +
                "\"username\":\"aValidUsername\"," +
                "\"logo\":\"http://www.logo.com\"," +
                "\"clientId\":\"12345\"," +
                "\"merchantName\":\"Test Merchant\"," +
                "\"pispName\":\"Test PISP\"," +
                "\"paymentReference\":\"A Payment reference\"," +
                "\"decisionApiUri\":\"/api/rcs/consent/decision/\"," +
                "\"intentType\":\"PAYMENT_DOMESTIC_CONSENT\"" +
                "}";
    }
}