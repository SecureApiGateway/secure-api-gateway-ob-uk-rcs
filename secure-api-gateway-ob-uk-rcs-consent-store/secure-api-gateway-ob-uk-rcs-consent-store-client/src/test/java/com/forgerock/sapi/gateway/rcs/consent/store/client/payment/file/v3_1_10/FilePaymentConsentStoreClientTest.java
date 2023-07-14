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
package com.forgerock.sapi.gateway.rcs.consent.store.client.payment.file.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.payment.file.v3_1_10.FilePaymentConsentValidationHelpers.validateAuthorisedConsent;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.payment.file.v3_1_10.FilePaymentConsentValidationHelpers.validateConsumedConsent;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.payment.file.v3_1_10.FilePaymentConsentValidationHelpers.validateCreateConsentAgainstCreateRequest;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.payment.file.v3_1_10.FilePaymentConsentValidationHelpers.validateRejectedConsent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.math.BigDecimal;
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
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteFileConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.TestConsentStoreClientConfigurationFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.CreateFilePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FilePaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FileUploadRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.file.v3_1_10.FilePaymentConsentValidationHelpers;

import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4Data.StatusEnum;
import uk.org.openbanking.testsupport.payment.OBWriteFileConsentTestDataFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"rcs.consent.store.api.baseUri= 'ignored'"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class FilePaymentConsentStoreClientTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private RestFilePaymentConsentStoreClient apiClient;

    @BeforeEach
    public void beforeEach() {
        apiClient = new RestFilePaymentConsentStoreClient(TestConsentStoreClientConfigurationFactory.createConsentStoreClientConfiguration(port), restTemplateBuilder, objectMapper);
    }

    @Test
    void testCreateConsent() {
        final CreateFilePaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FilePaymentConsent consent = apiClient.createConsent(createConsentRequest);

        validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);
    }

    @Test
    void failsToCreateConsentWhenFieldIsMissing() {
        final CreateFilePaymentConsentRequest requestMissingIdempotencyField = buildCreateConsentRequest();
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
    void testUploadFile() {
        final CreateFilePaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FilePaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final FileUploadRequest fileUploadRequest = buildFileUploadRequest(consent);
        final FilePaymentConsent consentWithFile = apiClient.uploadFile(fileUploadRequest);
        FilePaymentConsentValidationHelpers.validateConsentAgainstFileUploadRequest(consentWithFile, fileUploadRequest, consent);
    }

    @Test
    void testAuthoriseConsent() {
        final CreateFilePaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FilePaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final FileUploadRequest fileUploadRequest = buildFileUploadRequest(consent);
        final FilePaymentConsent consentWithFile = apiClient.uploadFile(fileUploadRequest);

        final AuthorisePaymentConsentRequest authRequest = buildAuthoriseConsentRequest(consent, "psu4test", "acc-12345");
        final FilePaymentConsent authResponse = apiClient.authoriseConsent(authRequest);

        validateAuthorisedConsent(authResponse, authRequest, consentWithFile);
    }

    @Test
    void testRejectConsent() {
        final CreateFilePaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FilePaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final FileUploadRequest fileUploadRequest = buildFileUploadRequest(consent);
        final FilePaymentConsent consentWithFile = apiClient.uploadFile(fileUploadRequest);

        final RejectConsentRequest rejectRequest = buildRejectRequest(consent, "joe.bloggs");
        final FilePaymentConsent rejectedConsent = apiClient.rejectConsent(rejectRequest);
        validateRejectedConsent(rejectedConsent, rejectRequest, consentWithFile);
    }

    @Test
    void testConsumeConsent() {
        final CreateFilePaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FilePaymentConsent consent = apiClient.createConsent(createConsentRequest);

        final FileUploadRequest fileUploadRequest = buildFileUploadRequest(consent);
        final FilePaymentConsent consentWithFile = apiClient.uploadFile(fileUploadRequest);

        final AuthorisePaymentConsentRequest authRequest = buildAuthoriseConsentRequest(consent, "psu4test", "acc-12345");
        final FilePaymentConsent authResponse = apiClient.authoriseConsent(authRequest);
        assertThat(authResponse.getStatus()).isEqualTo(StatusEnum.AUTHORISED.toString());

        final FilePaymentConsent consumedConsent = apiClient.consumeConsent(buildConsumeRequest(consent));
        assertThat(consumedConsent.getStatus()).isEqualTo(StatusEnum.CONSUMED.toString());

        validateConsumedConsent(consumedConsent, authResponse);
    }

    @Test
    void testGetConsent() {
        final CreateFilePaymentConsentRequest createConsentRequest = buildCreateConsentRequest();
        final FilePaymentConsent consent = apiClient.createConsent(createConsentRequest);
        final FilePaymentConsent getResponse = apiClient.getConsent(consent.getId(), consent.getApiClientId());
        assertThat(getResponse).usingRecursiveComparison().isEqualTo(consent);
    }

    private static CreateFilePaymentConsentRequest buildCreateConsentRequest() {
        final CreateFilePaymentConsentRequest createConsentRequest = new CreateFilePaymentConsentRequest();
        createConsentRequest.setIdempotencyKey(UUID.randomUUID().toString());
        createConsentRequest.setApiClientId("test-client-1");
        createConsentRequest.setConsentRequest(FRWriteFileConsentConverter.toFRWriteFileConsent(OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3("fileType", "hash", "23", BigDecimal.ONE)));
        createConsentRequest.setCharges(List.of(
                FRCharge.builder().type("fee")
                        .chargeBearer(FRChargeBearerType.BORNEBYCREDITOR)
                        .amount(new FRAmount("1.25","GBP"))
                        .build()));
        return createConsentRequest;
    }

    private static FileUploadRequest buildFileUploadRequest(FilePaymentConsent filePaymentConsent) {
        final FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setFileContents("<xml>blahblahblah</xml>");
        fileUploadRequest.setFileUploadIdempotencyKey(UUID.randomUUID().toString());
        fileUploadRequest.setConsentId(filePaymentConsent.getId());
        fileUploadRequest.setApiClientId(filePaymentConsent.getApiClientId());
        return fileUploadRequest;
    }

    private static AuthorisePaymentConsentRequest buildAuthoriseConsentRequest(FilePaymentConsent consent, String resourceOwnerId, String authorisedDebtorAccountId) {
        final AuthorisePaymentConsentRequest authRequest = new AuthorisePaymentConsentRequest();
        authRequest.setAuthorisedDebtorAccountId(authorisedDebtorAccountId);
        authRequest.setConsentId(consent.getId());
        authRequest.setResourceOwnerId(resourceOwnerId);
        authRequest.setApiClientId(consent.getApiClientId());
        return authRequest;
    }

    private static RejectConsentRequest buildRejectRequest(FilePaymentConsent consent, String resourceOwnerId) {
        final RejectConsentRequest rejectRequest = new RejectConsentRequest();
        rejectRequest.setApiClientId(consent.getApiClientId());
        rejectRequest.setConsentId(consent.getId());
        rejectRequest.setResourceOwnerId(resourceOwnerId);
        return rejectRequest;
    }

    private static ConsumePaymentConsentRequest buildConsumeRequest(FilePaymentConsent consent) {
        final ConsumePaymentConsentRequest consumeRequest = new ConsumePaymentConsentRequest();
        consumeRequest.setApiClientId(consent.getApiClientId());
        consumeRequest.setConsentId((consent.getId()));
        return consumeRequest;
    }

}