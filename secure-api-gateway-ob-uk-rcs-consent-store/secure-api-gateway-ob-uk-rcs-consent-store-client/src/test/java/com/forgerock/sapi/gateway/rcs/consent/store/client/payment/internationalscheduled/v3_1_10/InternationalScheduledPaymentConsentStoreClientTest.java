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
package com.forgerock.sapi.gateway.rcs.consent.store.client.payment.internationalscheduled.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.payment.PaymentConsentValidationHelpers.validateAuthorisedConsent;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.payment.PaymentConsentValidationHelpers.validateConsumedConsent;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.payment.PaymentConsentValidationHelpers.validateCreateConsentAgainstCreateRequest;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.payment.PaymentConsentValidationHelpers.validateRejectedConsent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRChargeBearerType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalScheduledConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.TestConsentStoreClientConfigurationFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.CreateInternationalScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.InternationalScheduledPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international.BasePaymentServiceWithExchangeRateInformationTest;

import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsent5;
import uk.org.openbanking.testsupport.payment.OBWriteInternationalScheduledConsentTestDataFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"rcs.consent.store.api.baseUri= 'ignored'"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class InternationalScheduledPaymentConsentStoreClientTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private RestInternationalScheduledPaymentConsentStoreClient apiClient;

    @BeforeEach
    public void beforeEach() {
        apiClient = new RestInternationalScheduledPaymentConsentStoreClient(TestConsentStoreClientConfigurationFactory.createConsentStoreClientConfiguration(port), restTemplateBuilder, objectMapper);
    }

    @Test
    void testCreateConsent() {
        final CreateInternationalScheduledPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final InternationalScheduledPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);
    }

    @Test
    void failsToCreateConsentWhenFieldIsMissing() {
        final CreateInternationalScheduledPaymentConsentRequest requestMissingIdempotencyField = buildCreateConsentRequest();
        requestMissingIdempotencyField.setIdempotencyKey(null);

        final ConsentStoreClientException clientException = assertThrows(ConsentStoreClientException.class,
                () -> apiClient.createConsent(requestMissingIdempotencyField));
        assertThat(clientException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(clientException.getObError1()).isNotNull();
        assertThat(clientException.getObError1().getErrorCode()).isEqualTo("UK.OBIE.Field.Invalid");
        assertThat(clientException.getObError1().getMessage()).isEqualTo("The field received is invalid. Reason 'must not be null'");
        assertThat(clientException.getObError1().getPath()).isEqualTo("idempotencyKey");
    }

    @Test
    void testAuthoriseConsent() {
        final CreateInternationalScheduledPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final InternationalScheduledPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final AuthorisePaymentConsentRequest authRequest = buildAuthoriseConsentRequest(consent, "psu4test", "acc-12345");
        final InternationalScheduledPaymentConsent authResponse = apiClient.authoriseConsent(authRequest);

        validateAuthorisedConsent(authResponse, authRequest, consent);
    }

    @Test
    void testRejectConsent() {
        final CreateInternationalScheduledPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final InternationalScheduledPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final RejectConsentRequest rejectRequest = buildRejectRequest(consent, "joe.bloggs");
        final InternationalScheduledPaymentConsent rejectedConsent = apiClient.rejectConsent(rejectRequest);
        validateRejectedConsent(rejectedConsent, rejectRequest, consent);
    }

    @Test
    void testConsumeConsent() {
        final CreateInternationalScheduledPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final InternationalScheduledPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final AuthorisePaymentConsentRequest authRequest = buildAuthoriseConsentRequest(consent, "psu4test", "acc-12345");
        final InternationalScheduledPaymentConsent authResponse = apiClient.authoriseConsent(authRequest);
        assertThat(authResponse.getStatus()).isEqualTo(StatusEnum.AUTHORISED.toString());

        final InternationalScheduledPaymentConsent consumedConsent = apiClient.consumeConsent(buildConsumeRequest(consent));
        assertThat(consumedConsent.getStatus()).isEqualTo(StatusEnum.CONSUMED.toString());

        validateConsumedConsent(consumedConsent, authResponse);
    }

    @Test
    void testGetConsent() {
        final CreateInternationalScheduledPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final InternationalScheduledPaymentConsent consent = apiClient.createConsent(createConsentRequest);
        final InternationalScheduledPaymentConsent getResponse = apiClient.getConsent(consent.getId(), consent.getApiClientId());
        assertThat(getResponse).usingRecursiveComparison().isEqualTo(consent);
    }

    private static CreateInternationalScheduledPaymentConsentRequest buildCreateConsentRequest() {
        final CreateInternationalScheduledPaymentConsentRequest createConsentRequest = new CreateInternationalScheduledPaymentConsentRequest();
        createConsentRequest.setIdempotencyKey(UUID.randomUUID().toString());
        createConsentRequest.setApiClientId("test-client-1");
        final OBWriteInternationalScheduledConsent5 obConsent = OBWriteInternationalScheduledConsentTestDataFactory.aValidOBWriteInternationalScheduledConsent5();
        createConsentRequest.setConsentRequest(FRWriteInternationalScheduledConsentConverter.toFRWriteInternationalScheduledConsent(obConsent));
        createConsentRequest.setCharges(List.of(
                FRCharge.builder().type("fee")
                        .chargeBearer(FRChargeBearerType.BORNEBYCREDITOR)
                        .amount(new FRAmount("1.25","GBP"))
                        .build()));
        createConsentRequest.setExchangeRateInformation(BasePaymentServiceWithExchangeRateInformationTest.getExchangeRateInformation(obConsent.getData().getInitiation().getExchangeRateInformation()));
        return createConsentRequest;
    }

    private static AuthorisePaymentConsentRequest buildAuthoriseConsentRequest(InternationalScheduledPaymentConsent consent, String resourceOwnerId, String authorisedDebtorAccountId) {
        final AuthorisePaymentConsentRequest authRequest = new AuthorisePaymentConsentRequest();
        authRequest.setAuthorisedDebtorAccountId(authorisedDebtorAccountId);
        authRequest.setConsentId(consent.getId());
        authRequest.setResourceOwnerId(resourceOwnerId);
        authRequest.setApiClientId(consent.getApiClientId());
        return authRequest;
    }

    private static RejectConsentRequest buildRejectRequest(InternationalScheduledPaymentConsent consent, String resourceOwnerId) {
        final RejectConsentRequest rejectRequest = new RejectConsentRequest();
        rejectRequest.setApiClientId(consent.getApiClientId());
        rejectRequest.setConsentId(consent.getId());
        rejectRequest.setResourceOwnerId(resourceOwnerId);
        return rejectRequest;
    }

    private static ConsumePaymentConsentRequest buildConsumeRequest(InternationalScheduledPaymentConsent consent) {
        final ConsumePaymentConsentRequest consumeRequest = new ConsumePaymentConsentRequest();
        consumeRequest.setApiClientId(consent.getApiClientId());
        consumeRequest.setConsentId((consent.getId()));
        return consumeRequest;
    }

}