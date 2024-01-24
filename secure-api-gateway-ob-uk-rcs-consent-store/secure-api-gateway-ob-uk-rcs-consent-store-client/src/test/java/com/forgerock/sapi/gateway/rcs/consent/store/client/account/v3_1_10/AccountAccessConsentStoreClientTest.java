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
package com.forgerock.sapi.gateway.rcs.consent.store.client.account.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.client.TestConsentStoreClientConfigurationFactory.createConsentStoreClientConfiguration;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.account.v3_1_10.AccountAccessConsentValidationHelpers.validateAuthorisedConsent;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.account.v3_1_10.AccountAccessConsentValidationHelpers.validateCreateConsentAgainstCreateRequest;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.account.v3_1_10.AccountAccessConsentValidationHelpers.validateRejectedConsent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AuthoriseAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;

import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadConsent1;
import uk.org.openbanking.datamodel.account.OBReadConsent1Data;
import uk.org.openbanking.datamodel.account.OBRisk2;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"rcs.consent.store.api.baseUri= 'ignored'"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class AccountAccessConsentStoreClientTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private RestAccountAccessConsentStoreClient apiClient;

    @BeforeEach
    public void beforeEach() {
        apiClient = new RestAccountAccessConsentStoreClient(createConsentStoreClientConfiguration(port), restTemplateBuilder, objectMapper);
    }

    @Test
    void testCreateConsent() {
        final CreateAccountAccessConsentRequest createConsentRequest = buildCreateConsentRequest();
        final AccountAccessConsent consent = apiClient.createConsent(createConsentRequest);

        validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);
    }

    @Test
    void failsToCreateConsentWhenFieldIsMissing() {
        final CreateAccountAccessConsentRequest requestMissingConsentReqField = new CreateAccountAccessConsentRequest();
        requestMissingConsentReqField.setApiClientId("test-client-1");

        final ConsentStoreClientException clientException = assertThrows(ConsentStoreClientException.class,
                () -> apiClient.createConsent(requestMissingConsentReqField));
        assertThat(clientException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(clientException.getObError1()).isNotNull();
        assertThat(clientException.getObError1().getErrorCode()).isEqualTo("UK.OBIE.Field.Invalid");
        assertThat(clientException.getObError1().getMessage()).isEqualTo("The field received is invalid. Reason 'must not be null'");
        assertThat(clientException.getObError1().getPath()).isEqualTo("consentRequest");
    }

    @Test
    void testAuthoriseConsent() {
        final CreateAccountAccessConsentRequest createConsentRequest = buildCreateConsentRequest();
        final AccountAccessConsent consent = apiClient.createConsent(createConsentRequest);

        final AuthoriseAccountAccessConsentRequest authRequest = buildAuthoriseConsentRequest(consent, "psu4test", List.of("acc-12345", "another-acc-adffds"));
        final AccountAccessConsent authResponse = apiClient.authoriseConsent(authRequest);

        validateAuthorisedConsent(authResponse, authRequest, consent);
    }

    @Test
    void testRejectConsent() {
        final CreateAccountAccessConsentRequest createConsentRequest = buildCreateConsentRequest();
        final AccountAccessConsent consent = apiClient.createConsent(createConsentRequest);

        final RejectConsentRequest rejectRequest = buildRejectRequest(consent, "joe.bloggs");
        final AccountAccessConsent rejectedConsent = apiClient.rejectConsent(rejectRequest);
        validateRejectedConsent(rejectedConsent, rejectRequest, consent);
    }

    @Test
    void testGetConsent() {
        final CreateAccountAccessConsentRequest createConsentRequest = buildCreateConsentRequest();
        final AccountAccessConsent consent = apiClient.createConsent(createConsentRequest);
        final AccountAccessConsent getResponse = apiClient.getConsent(consent.getId(), consent.getApiClientId());
        assertThat(getResponse).usingRecursiveComparison().isEqualTo(consent);
    }

    @Test
    void testDeleteConsent() {
        final CreateAccountAccessConsentRequest createConsentRequest = buildCreateConsentRequest();
        final AccountAccessConsent consent = apiClient.createConsent(createConsentRequest);
        apiClient.deleteConsent(consent.getId(), consent.getApiClientId());
    }

    private static CreateAccountAccessConsentRequest buildCreateConsentRequest() {
        final CreateAccountAccessConsentRequest createConsentRequest = new CreateAccountAccessConsentRequest();
        createConsentRequest.setApiClientId("test-client-1");
        createConsentRequest.setConsentRequest(FRReadConsentConverter.toFRReadConsent(new OBReadConsent1()
                                                    .data(new OBReadConsent1Data().permissions(List.of(OBExternalPermissions1Code.READACCOUNTSBASIC)))
                                                    .risk(new OBRisk2())));
        return createConsentRequest;
    }

    private static AuthoriseAccountAccessConsentRequest buildAuthoriseConsentRequest(AccountAccessConsent consent, String resourceOwnerId, List<String> authorisedAccounts) {
        final AuthoriseAccountAccessConsentRequest authRequest = new AuthoriseAccountAccessConsentRequest();
        authRequest.setAuthorisedAccountIds(authorisedAccounts);
        authRequest.setConsentId(consent.getId());
        authRequest.setResourceOwnerId(resourceOwnerId);
        authRequest.setApiClientId(consent.getApiClientId());
        return authRequest;
    }

    private static RejectConsentRequest buildRejectRequest(AccountAccessConsent consent, String resourceOwnerId) {
        final RejectConsentRequest rejectRequest = new RejectConsentRequest();
        rejectRequest.setApiClientId(consent.getApiClientId());
        rejectRequest.setConsentId(consent.getId());
        rejectRequest.setResourceOwnerId(resourceOwnerId);
        return rejectRequest;
    }

}
