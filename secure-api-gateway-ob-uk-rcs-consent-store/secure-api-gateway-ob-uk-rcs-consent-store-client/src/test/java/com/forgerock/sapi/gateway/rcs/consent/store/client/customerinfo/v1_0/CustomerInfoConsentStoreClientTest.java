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
package com.forgerock.sapi.gateway.rcs.consent.store.client.customerinfo.v1_0;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.AuthoriseCustomerInfoConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.CreateCustomerInfoConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.CustomerInfoConsent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadConsent1;
import uk.org.openbanking.datamodel.account.OBReadData1;
import uk.org.openbanking.datamodel.account.OBRisk2;

import java.util.List;
import java.util.UUID;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.customerinfo.v1_0.CustomerInfoConsentValidationHelpers.*;
import static com.forgerock.sapi.gateway.rcs.consent.store.client.TestConsentStoreClientConfigurationFactory.createConsentStoreClientConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Test for {@link CustomerInfoConsentStoreClient}
 */
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"rcs.consent.store.api.baseUri= 'ignored'"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class CustomerInfoConsentStoreClientTest {

    private static final String API_CLIENT_ID = UUID.randomUUID().toString();
    private static final String RESOURCE_OWNER_ID = UUID.randomUUID().toString();

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private RestCustomerInfoConsentStoreClient storeApiClient;

    @BeforeEach
    public void beforeEach() {
        storeApiClient = new RestCustomerInfoConsentStoreClient(createConsentStoreClientConfiguration(port), restTemplateBuilder, objectMapper);
    }

    @Test
    void testCreateConsent() {
        final CreateCustomerInfoConsentRequest createConsentRequest = buildCreateConsentRequest();
        final CustomerInfoConsent consent = storeApiClient.createConsent(createConsentRequest);

        validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);
    }

    @Test
    void failsToCreateConsentWhenFieldIsMissing() {
        final CreateCustomerInfoConsentRequest requestMissingConsentReqField = new CreateCustomerInfoConsentRequest();
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
        final CreateCustomerInfoConsentRequest createConsentRequest = buildCreateConsentRequest();
        final CustomerInfoConsent consent = storeApiClient.createConsent(createConsentRequest);

        final AuthoriseCustomerInfoConsentRequest authRequest = buildAuthoriseConsentRequest(consent);
        final CustomerInfoConsent authResponse = storeApiClient.authoriseConsent(authRequest);

        validateAuthorisedConsent(authResponse, authRequest, consent);
    }

    @Test
    void testRejectConsent() {
        final CreateCustomerInfoConsentRequest createConsentRequest = buildCreateConsentRequest();
        final CustomerInfoConsent consent = storeApiClient.createConsent(createConsentRequest);

        final RejectConsentRequest rejectRequest = buildRejectRequest(consent);
        final CustomerInfoConsent rejectedConsent = storeApiClient.rejectConsent(rejectRequest);
        validateRejectedConsent(rejectedConsent, rejectRequest, consent);
    }

    @Test
    void testGetConsent() {
        final CreateCustomerInfoConsentRequest createConsentRequest = buildCreateConsentRequest();
        final CustomerInfoConsent consent = storeApiClient.createConsent(createConsentRequest);
        final CustomerInfoConsent getResponse = storeApiClient.getConsent(consent.getId(), consent.getApiClientId());
        assertThat(getResponse).usingRecursiveComparison().isEqualTo(consent);
    }

    @Test
    void testDeleteConsent() {
        final CreateCustomerInfoConsentRequest createConsentRequest = buildCreateConsentRequest();
        final CustomerInfoConsent consent = storeApiClient.createConsent(createConsentRequest);
        storeApiClient.deleteConsent(consent.getId(), consent.getApiClientId());
    }

    private static CreateCustomerInfoConsentRequest buildCreateConsentRequest() {
        final CreateCustomerInfoConsentRequest createConsentRequest = new CreateCustomerInfoConsentRequest();
        createConsentRequest.setApiClientId(API_CLIENT_ID);
        createConsentRequest.setConsentRequest(FRReadConsentConverter.toFRReadConsent(new OBReadConsent1()
                .data(new OBReadData1().permissions(List.of(OBExternalPermissions1Code.READCUSTOMERINFO)))
                .risk(new OBRisk2())));
        return createConsentRequest;
    }

    private static AuthoriseCustomerInfoConsentRequest buildAuthoriseConsentRequest(CustomerInfoConsent consent) {
        final AuthoriseCustomerInfoConsentRequest authRequest = new AuthoriseCustomerInfoConsentRequest();
        authRequest.setConsentId(consent.getId());
        authRequest.setResourceOwnerId(RESOURCE_OWNER_ID);
        authRequest.setApiClientId(consent.getApiClientId());
        return authRequest;
    }

    private static RejectConsentRequest buildRejectRequest(CustomerInfoConsent consent) {
        final RejectConsentRequest rejectRequest = new RejectConsentRequest();
        rejectRequest.setApiClientId(consent.getApiClientId());
        rejectRequest.setConsentId(consent.getId());
        rejectRequest.setResourceOwnerId(RESOURCE_OWNER_ID);
        return rejectRequest;
    }
}
