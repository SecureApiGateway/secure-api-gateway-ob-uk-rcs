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
package com.forgerock.sapi.gateway.rcs.consent.store.client.funds.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.funds.v3_1_10.FundsConfirmationConsentValidationHelpers.validateAuthorisedConsent;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.funds.v3_1_10.FundsConfirmationConsentValidationHelpers.validateCreateConsentAgainstCreateRequest;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.funds.v3_1_10.FundsConfirmationConsentValidationHelpers.validateRejectedConsent;
import static com.forgerock.sapi.gateway.rcs.consent.store.client.TestConsentStoreClientConfigurationFactory.createConsentStoreClientConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.funds.FRFundsConfirmationConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.AuthoriseFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.CreateFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;

import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsent1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsent1Data;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsent1DataDebtorAccount;

/**
 * Test for {@link FundsConfirmationConsentStoreClient}
 */
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"rcs.consent.store.api.baseUri= 'ignored'"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class FundsConfirmationConsentStoreClientTest {

    private static final String API_CLIENT_ID = UUID.randomUUID().toString();
    private static final String RESOURCE_OWNER_ID = UUID.randomUUID().toString();

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private RestFundsConfirmationConsentStoreClient storeApiClient;

    @BeforeEach
    public void beforeEach() {
        storeApiClient = new RestFundsConfirmationConsentStoreClient(createConsentStoreClientConfiguration(port), restTemplateBuilder, objectMapper);
    }

    @Test
    void testCreateConsent() {
        final CreateFundsConfirmationConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FundsConfirmationConsent consent = storeApiClient.createConsent(createConsentRequest);

        validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);
    }

    @Test
    void failsToCreateConsentWhenFieldIsMissing() {
        final CreateFundsConfirmationConsentRequest requestMissingConsentReqField = new CreateFundsConfirmationConsentRequest();
        requestMissingConsentReqField.setApiClientId(API_CLIENT_ID);

        final ConsentStoreClientException clientException = assertThrows(ConsentStoreClientException.class,
                () -> storeApiClient.createConsent(requestMissingConsentReqField));
        assertThat(clientException.getErrorType()).isEqualTo(ConsentStoreClientException.ErrorType.BAD_REQUEST);
        assertThat(clientException.getObError1()).isNotNull();
        assertThat(clientException.getObError1().getErrorCode()).isEqualTo("UK.OBIE.Field.Invalid");
        assertThat(clientException.getObError1().getMessage()).isEqualTo("The field received is invalid. Reason 'must not be null'");
        assertThat(clientException.getObError1().getPath()).isEqualTo("consentRequest");
    }

    @Test
    void testAuthoriseConsent() {
        final CreateFundsConfirmationConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FundsConfirmationConsent consent = storeApiClient.createConsent(createConsentRequest);

        final AuthoriseFundsConfirmationConsentRequest authRequest = buildAuthoriseConsentRequest(consent);
        final FundsConfirmationConsent authResponse = storeApiClient.authoriseConsent(authRequest);

        validateAuthorisedConsent(authResponse, authRequest, consent);
    }

    @Test
    void testRejectConsent() {
        final CreateFundsConfirmationConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FundsConfirmationConsent consent = storeApiClient.createConsent(createConsentRequest);

        final RejectConsentRequest rejectRequest = buildRejectRequest(consent);
        final FundsConfirmationConsent rejectedConsent = storeApiClient.rejectConsent(rejectRequest);
        validateRejectedConsent(rejectedConsent, rejectRequest, consent);
    }

    @Test
    void testGetConsent() {
        final CreateFundsConfirmationConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FundsConfirmationConsent consent = storeApiClient.createConsent(createConsentRequest);
        final FundsConfirmationConsent getResponse = storeApiClient.getConsent(consent.getId(), consent.getApiClientId());
        assertThat(getResponse).usingRecursiveComparison().isEqualTo(consent);
    }

    @Test
    void testDeleteConsent() {
        final CreateFundsConfirmationConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FundsConfirmationConsent consent = storeApiClient.createConsent(createConsentRequest);
        storeApiClient.deleteConsent(consent.getId(), consent.getApiClientId());
    }

    private static CreateFundsConfirmationConsentRequest buildCreateConsentRequest() {
        final CreateFundsConfirmationConsentRequest createConsentRequest = new CreateFundsConfirmationConsentRequest();
        createConsentRequest.setApiClientId(API_CLIENT_ID);
        final OBFundsConfirmationConsent1 fundsConfirmationConsent1 = new OBFundsConfirmationConsent1();
        fundsConfirmationConsent1.setData(
                new OBFundsConfirmationConsent1Data()
                        .expirationDateTime(DateTime.now().plusDays(30))
                        .debtorAccount(
                                new OBFundsConfirmationConsent1DataDebtorAccount()
                                        .schemeName("UK.OBIE.SortCodeAccountNumber")
                                        .identification("40400422390112")
                                        .name("Mrs B Smith")
                        )
        );
        createConsentRequest.setConsentRequest(FRFundsConfirmationConsentConverter.toFRFundsConfirmationConsent(fundsConfirmationConsent1));
        return createConsentRequest;
    }

    private static AuthoriseFundsConfirmationConsentRequest buildAuthoriseConsentRequest(FundsConfirmationConsent consent) {
        final AuthoriseFundsConfirmationConsentRequest authRequest = new AuthoriseFundsConfirmationConsentRequest();
        authRequest.setConsentId(consent.getId());
        authRequest.setResourceOwnerId(RESOURCE_OWNER_ID);
        authRequest.setApiClientId(consent.getApiClientId());
        authRequest.setAuthorisedDebtorAccountId(UUID.randomUUID().toString());
        return authRequest;
    }

    private static RejectConsentRequest buildRejectRequest(FundsConfirmationConsent consent) {
        final RejectConsentRequest rejectRequest = new RejectConsentRequest();
        rejectRequest.setApiClientId(consent.getApiClientId());
        rejectRequest.setConsentId(consent.getId());
        rejectRequest.setResourceOwnerId(RESOURCE_OWNER_ID);
        return rejectRequest;
    }
}
