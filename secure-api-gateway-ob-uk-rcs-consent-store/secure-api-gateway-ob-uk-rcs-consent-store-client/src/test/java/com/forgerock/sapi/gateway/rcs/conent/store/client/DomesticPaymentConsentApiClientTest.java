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
package com.forgerock.sapi.gateway.rcs.conent.store.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.AuthoriseDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.ConsumeDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.RejectDomesticPaymentConsentRequest;

import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.common.OBChargeBearerType1Code;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5DataCharges;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"rcs.consent.store.api.baseUrl= 'ignored'"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class DomesticPaymentConsentApiClientTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    private RestDomesticPaymentConsentApiClient apiClient;

    @BeforeEach
    public void beforeEach() {
        final ConsentServiceClientConfiguration clientConfiguration = new ConsentServiceClientConfiguration();
        clientConfiguration.setBaseUrl("http://localhost:"+port+"/consent/store");
        apiClient = new RestDomesticPaymentConsentApiClient(clientConfiguration, restTemplate);
    }


    @Test
    void testCreateConsent() {
        final CreateDomesticPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final DomesticPaymentConsent consent = apiClient.createConsent(createConsentRequest);
        assertThat(consent.getId()).isNotNull();

        // TODO more assertions
    }

    @Test
    void failsToCreateConsentWhenFieldIsMissing() {
        final CreateDomesticPaymentConsentRequest requestMissingIdempotencyFields = new CreateDomesticPaymentConsentRequest();
        requestMissingIdempotencyFields.setApiClientId("test-client-1");
        requestMissingIdempotencyFields.setConsentRequest(OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4());
        requestMissingIdempotencyFields.setCharges(List.of(new OBWriteDomesticConsentResponse5DataCharges().type("fee").chargeBearer(OBChargeBearerType1Code.BORNEBYCREDITOR).amount(new OBActiveOrHistoricCurrencyAndAmount().amount("1.25").currency("GBP"))));

        final HttpClientErrorException httpClientErrorException = assertThrows(HttpClientErrorException.class,
                () -> apiClient.createConsent(requestMissingIdempotencyFields));
        // TODO better error handling
    }

    @Test
    void testAuthoriseConsent() {
        final CreateDomesticPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final DomesticPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final AuthoriseDomesticPaymentConsentRequest authRequest = buildAuthoriseConsentRequest(consent, "psu4test", "acc-12345");
        final DomesticPaymentConsent authResponse = apiClient.authoriseConsent(authRequest);
        assertThat(authResponse.getStatus()).isEqualTo(StatusEnum.AUTHORISED.toString());
        // TODO more assertions
    }

    @Test
    void testRejectConsent() {
        final CreateDomesticPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final DomesticPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final RejectDomesticPaymentConsentRequest rejectRequest = buildRejectRequest(consent, "joe.bloggs");
        final DomesticPaymentConsent rejectedConsent = apiClient.rejectConsent(rejectRequest);
        assertThat(rejectedConsent.getStatus()).isEqualTo(StatusEnum.REJECTED.toString());
    }

    @Test
    void testConsumeConsent() {
        final CreateDomesticPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final DomesticPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final AuthoriseDomesticPaymentConsentRequest authRequest = buildAuthoriseConsentRequest(consent, "psu4test", "acc-12345");
        final DomesticPaymentConsent authResponse = apiClient.authoriseConsent(authRequest);
        assertThat(authResponse.getStatus()).isEqualTo(StatusEnum.AUTHORISED.toString());

        final DomesticPaymentConsent consumedConsent = apiClient.consumeConsent(buildConsumeRequest(consent));
        assertThat(consumedConsent.getStatus()).isEqualTo(StatusEnum.CONSUMED.toString());
    }

    @Test
    void testGetConsent() {
        final CreateDomesticPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final DomesticPaymentConsent consent = apiClient.createConsent(createConsentRequest);
        final DomesticPaymentConsent getResponse = apiClient.getConsent(consent.getId(), consent.getApiClientId());
        assertThat(getResponse).usingRecursiveComparison().isEqualTo(consent);
    }

    private static CreateDomesticPaymentConsentRequest buildCreateConsentRequest() {
        final CreateDomesticPaymentConsentRequest createConsentRequest = new CreateDomesticPaymentConsentRequest();
        createConsentRequest.setIdempotencyKey(UUID.randomUUID().toString());
        createConsentRequest.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        createConsentRequest.setApiClientId("test-client-1");
        createConsentRequest.setConsentRequest(OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4());
        createConsentRequest.setCharges(List.of(new OBWriteDomesticConsentResponse5DataCharges().type("fee").chargeBearer(OBChargeBearerType1Code.BORNEBYCREDITOR).amount(new OBActiveOrHistoricCurrencyAndAmount().amount("1.25").currency("GBP"))));
        return createConsentRequest;
    }

    private static AuthoriseDomesticPaymentConsentRequest buildAuthoriseConsentRequest(DomesticPaymentConsent consent, String resourceOwnerId, String authorisedDebtorAccountId) {
        final AuthoriseDomesticPaymentConsentRequest authRequest = new AuthoriseDomesticPaymentConsentRequest();
        authRequest.setAuthorisedDebtorAccountId(authorisedDebtorAccountId);
        authRequest.setConsentId(consent.getId());
        authRequest.setResourceOwnerId(resourceOwnerId);
        authRequest.setApiClientId(consent.getApiClientId());
        return authRequest;
    }

    private static RejectDomesticPaymentConsentRequest buildRejectRequest(DomesticPaymentConsent consent, String resourceOwnerId) {
        final RejectDomesticPaymentConsentRequest rejectRequest = new RejectDomesticPaymentConsentRequest();
        rejectRequest.setApiClientId(consent.getApiClientId());
        rejectRequest.setConsentId(consent.getId());
        rejectRequest.setResourceOwnerId(resourceOwnerId);
        return rejectRequest;
    }

    private static ConsumeDomesticPaymentConsentRequest buildConsumeRequest(DomesticPaymentConsent consent) {
        final ConsumeDomesticPaymentConsentRequest consumeRequest = new ConsumeDomesticPaymentConsentRequest();
        consumeRequest.setApiClientId(consent.getApiClientId());
        consumeRequest.setConsentId((consent.getId()));
        return consumeRequest;
    }

}