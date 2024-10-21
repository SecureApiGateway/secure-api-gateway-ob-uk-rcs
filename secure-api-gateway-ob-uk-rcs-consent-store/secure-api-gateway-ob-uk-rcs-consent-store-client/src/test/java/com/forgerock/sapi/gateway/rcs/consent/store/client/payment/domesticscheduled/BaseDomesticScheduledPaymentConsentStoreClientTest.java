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
package com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticscheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRChargeBearerType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticScheduledConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.CreateDomesticScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticScheduledConsentTestDataFactory;

import java.util.List;
import java.util.UUID;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.payment.PaymentConsentValidationHelpers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"rcs.consent.store.api.baseUri= 'ignored'"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public abstract class BaseDomesticScheduledPaymentConsentStoreClientTest {

    protected final OBVersion version;

    @LocalServerPort
    protected int port;

    @Autowired
    protected RestTemplateBuilder restTemplateBuilder;

    @Autowired
    protected ObjectMapper objectMapper;

    protected BaseRestDomesticScheduledPaymentConsentStoreClient apiClient;

    protected BaseDomesticScheduledPaymentConsentStoreClientTest(OBVersion version) {
        this.version = version;
    }

    @BeforeEach
    public void beforeEach() {
        apiClient = createApiClient();
    }

    protected abstract BaseRestDomesticScheduledPaymentConsentStoreClient createApiClient();

    @Test
    void testCreateConsent() {
        final CreateDomesticScheduledPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final DomesticScheduledPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        validateCreateConsentAgainstCreateRequest(consent, createConsentRequest, version);
    }

    @Test
    void failsToCreateConsentWhenFieldIsMissing() {
        final CreateDomesticScheduledPaymentConsentRequest requestMissingIdempotencyField = buildCreateConsentRequest();
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
        final CreateDomesticScheduledPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final DomesticScheduledPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final AuthorisePaymentConsentRequest authRequest = buildAuthoriseConsentRequest(consent, "psu4test", "acc-12345");
        final DomesticScheduledPaymentConsent authResponse = apiClient.authoriseConsent(authRequest);

        validateAuthorisedConsent(authResponse, authRequest, consent);
    }

    @Test
    void testRejectConsent() {
        final CreateDomesticScheduledPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final DomesticScheduledPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final RejectConsentRequest rejectRequest = buildRejectRequest(consent, "joe.bloggs");
        final DomesticScheduledPaymentConsent rejectedConsent = apiClient.rejectConsent(rejectRequest);
        validateRejectedConsent(rejectedConsent, rejectRequest, consent);
    }

    @Test
    void testConsumeConsent() {
        final CreateDomesticScheduledPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final DomesticScheduledPaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final AuthorisePaymentConsentRequest authRequest = buildAuthoriseConsentRequest(consent, "psu4test", "acc-12345");
        final DomesticScheduledPaymentConsent authResponse = apiClient.authoriseConsent(authRequest);
        assertThat(authResponse.getStatus()).isEqualTo(OBPaymentConsentStatus.AUTHORISED.toString());

        final DomesticScheduledPaymentConsent consumedConsent = apiClient.consumeConsent(buildConsumeRequest(consent));
        assertThat(consumedConsent.getStatus()).isEqualTo(OBPaymentConsentStatus.CONSUMED.toString());

        validateConsumedConsent(consumedConsent, authResponse);
    }

    @Test
    void testGetConsent() {
        final CreateDomesticScheduledPaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final DomesticScheduledPaymentConsent consent = apiClient.createConsent(createConsentRequest);
        final DomesticScheduledPaymentConsent getResponse = apiClient.getConsent(consent.getId(), consent.getApiClientId());
        assertThat(getResponse).usingRecursiveComparison().isEqualTo(consent);
    }

    private static CreateDomesticScheduledPaymentConsentRequest buildCreateConsentRequest() {
        final CreateDomesticScheduledPaymentConsentRequest createConsentRequest = new CreateDomesticScheduledPaymentConsentRequest();
        createConsentRequest.setIdempotencyKey(UUID.randomUUID().toString());
        createConsentRequest.setApiClientId("test-client-1");
        createConsentRequest.setCharges(List.of(
                FRCharge.builder().type("fee")
                        .chargeBearer(FRChargeBearerType.BORNEBYCREDITOR)
                        .amount(new FRAmount("1.25","GBP"))
                        .build()));
        createConsentRequest.setConsentRequest(FRWriteDomesticScheduledConsentConverter.toFRWriteDomesticScheduledConsent(OBWriteDomesticScheduledConsentTestDataFactory.aValidOBWriteDomesticScheduledConsent4()));
        return createConsentRequest;
    }

    private static AuthorisePaymentConsentRequest buildAuthoriseConsentRequest(DomesticScheduledPaymentConsent consent, String resourceOwnerId, String authorisedDebtorAccountId) {
        final AuthorisePaymentConsentRequest authRequest = new AuthorisePaymentConsentRequest();
        authRequest.setAuthorisedDebtorAccountId(authorisedDebtorAccountId);
        authRequest.setConsentId(consent.getId());
        authRequest.setResourceOwnerId(resourceOwnerId);
        authRequest.setApiClientId(consent.getApiClientId());
        return authRequest;
    }

    private static RejectConsentRequest buildRejectRequest(DomesticScheduledPaymentConsent consent, String resourceOwnerId) {
        final RejectConsentRequest rejectRequest = new RejectConsentRequest();
        rejectRequest.setApiClientId(consent.getApiClientId());
        rejectRequest.setConsentId(consent.getId());
        rejectRequest.setResourceOwnerId(resourceOwnerId);
        return rejectRequest;
    }

    private static ConsumePaymentConsentRequest buildConsumeRequest(DomesticScheduledPaymentConsent consent) {
        final ConsumePaymentConsentRequest consumeRequest = new ConsumePaymentConsentRequest();
        consumeRequest.setApiClientId(consent.getApiClientId());
        consumeRequest.setConsentId((consent.getId()));
        return consumeRequest;
    }

}