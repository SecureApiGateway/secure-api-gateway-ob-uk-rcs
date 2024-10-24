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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.file.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.ApiTestUtils.createConsentStoreApiRequiredHeaders;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRWriteFileConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteFileConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.CreateFilePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FilePaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FileUploadRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.file.FilePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.file.FilePaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v3.payment.OBWriteFileConsent3;
import uk.org.openbanking.testsupport.v3.payment.OBWriteFileConsentTestDataFactory;


public class FilePaymentConsentApiControllerTest extends BasePaymentConsentApiControllerTest<FilePaymentConsent, CreateFilePaymentConsentRequest> {

    @Autowired
    @Qualifier("internalFilePaymentConsentService")
    private FilePaymentConsentService consentService;

    public FilePaymentConsentApiControllerTest() {
        super(FilePaymentConsent.class);
    }

    @Override
    protected OBVersion getControllerVersion() {
        return OBVersion.v3_1_10;
    }

    @Override
    protected String getControllerEndpointName() {
        return "file-payment-consents";
    }

    @Override
    protected String createConsentEntityForVersionValidation(String apiClient, OBVersion version) {
        final FilePaymentConsentEntity consent = new FilePaymentConsentEntity();
        consent.setApiClientId(apiClient);
        consent.setRequestVersion(version);
        consent.setRequestObj(createFRConsent());
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusMinutes(5));
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        return consentService.createConsent(consent).getId();
    }

    @Override
    protected CreateFilePaymentConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateFilePaymentConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateFilePaymentConsentRequest buildCreateFilePaymentConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateFilePaymentConsentRequest createFilePaymentConsentRequest = new CreateFilePaymentConsentRequest();
        final FRWriteFileConsent frWriteFileConsent = createFRConsent();
        createFilePaymentConsentRequest.setConsentRequest(frWriteFileConsent);
        createFilePaymentConsentRequest.setApiClientId(apiClientId);
        createFilePaymentConsentRequest.setIdempotencyKey(idempotencyKey);
        return createFilePaymentConsentRequest;
    }

    private static FRWriteFileConsent createFRConsent() {
        final OBWriteFileConsent3 paymentConsent = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3("xml", "hash", "231", BigDecimal.ONE);
        final FRWriteFileConsent frWriteFileConsent = FRWriteFileConsentConverter.toFRWriteFileConsent(paymentConsent);
        return frWriteFileConsent;
    }

    @Override
    protected void validateCreateConsentAgainstCreateRequest(FilePaymentConsent consent, CreateFilePaymentConsentRequest createConsentRequest) {
        FilePaymentConsentValidationHelpers.validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);
    }

    @Override
    protected void validateRejectedConsent(FilePaymentConsent rejectedConsent, RejectConsentRequest rejectConsentRequest, FilePaymentConsent originalConsent) {
        FilePaymentConsentValidationHelpers.validateRejectedConsent(rejectedConsent, rejectConsentRequest, originalConsent);
    }

    @Override
    protected void validateAuthorisedConsent(FilePaymentConsent authorisedConsent, AuthorisePaymentConsentRequest authoriseConsentReq, FilePaymentConsent originalConsent) {
        FilePaymentConsentValidationHelpers.validateAuthorisedConsent(authorisedConsent, authoriseConsentReq, originalConsent);
    }

    @Override
    protected void validateConsumedConsent(FilePaymentConsent consumedConsent, FilePaymentConsent authorisedConsent) {
        FilePaymentConsentValidationHelpers.validateConsumedConsent(consumedConsent, authorisedConsent);
    }

    @Override
    protected FilePaymentConsent getConsentInStateToAuthoriseOrReject(String apiClientId) {
        return uploadFile(createConsent(apiClientId));
    }

    private FilePaymentConsent uploadFile(FilePaymentConsent consent) {
        return uploadFile(consent, getValidFileUploadRequest(consent));
    }

    private FilePaymentConsent uploadFile(FilePaymentConsent consent, FileUploadRequest fileUploadRequest) {
        final ResponseEntity<FilePaymentConsent> fileUploadResponse = uploadFile(fileUploadRequest, FilePaymentConsent.class);
        assertThat(fileUploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        final FilePaymentConsent consentWithFile = fileUploadResponse.getBody();
        FilePaymentConsentValidationHelpers.validateConsentAgainstFileUploadRequest(consentWithFile, fileUploadRequest, consent);
        return consentWithFile;
    }

    private <R> ResponseEntity<R> uploadFile(FileUploadRequest fileUploadRequest, Class<R> responseClass) {
        return restTemplate.exchange(apiBaseUrl + "/" + fileUploadRequest.getConsentId() + "/file" , HttpMethod.POST,
                new HttpEntity<>(fileUploadRequest, createConsentStoreApiRequiredHeaders(fileUploadRequest.getApiClientId())),
                responseClass);
    }

    protected FileUploadRequest getValidFileUploadRequest(FilePaymentConsent filePaymentConsent) {
        final FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setFileContents("<xml>dfsfsdf</xml>");
        fileUploadRequest.setConsentId(filePaymentConsent.getId());
        fileUploadRequest.setApiClientId(filePaymentConsent.getApiClientId());
        fileUploadRequest.setFileUploadIdempotencyKey(UUID.randomUUID().toString());
        return fileUploadRequest;
    }

    @Test
    void uploadFile() {
        uploadFile(createConsent(TEST_API_CLIENT_1));
    }

    @Test
    void uploadFileShouldBeIdempotent() {
        final FilePaymentConsent consent = createConsent(TEST_API_CLIENT_1);
        final FileUploadRequest validFileUploadRequest = getValidFileUploadRequest(consent);

        final FilePaymentConsent firstFileUploadResult = uploadFile(consent, validFileUploadRequest);

        for (int i = 0 ; i < 5; i++) {
            assertThat(uploadFile(consent, validFileUploadRequest)).usingRecursiveComparison().isEqualTo(firstFileUploadResult);
        }
    }

    @Test
    void failToUploadFileDifferentApiClient() {
        final FilePaymentConsent consent = createConsent(TEST_API_CLIENT_1);

        final FileUploadRequest fileUploadRequest = getValidFileUploadRequest(consent);
        fileUploadRequest.setApiClientId("different-client-id");

        final ResponseEntity<OBErrorResponse1> errorResult = uploadFile(fileUploadRequest, OBErrorResponse1.class);
        validateInvalidPermissionsErrorResponse(consent.getId(), errorResult);
    }

    @Test
    void failToUploadFileMissingFile() {
        final FilePaymentConsent consent = createConsent(TEST_API_CLIENT_1);

        final FileUploadRequest fileUploadRequest = getValidFileUploadRequest(consent);
        fileUploadRequest.setFileContents(null);

        final ResponseEntity<OBErrorResponse1> errorResponse = uploadFile(fileUploadRequest, OBErrorResponse1.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final OBErrorResponse1 obErrorResponse = errorResponse.getBody();
        assertThat(obErrorResponse.getCode()).isEqualTo("OBRI.Argument.Invalid");
        assertThat(obErrorResponse.getErrors()).isNotEmpty().
                contains(new OBError1().errorCode("UK.OBIE.Field.Invalid")
                        .message("The field received is invalid. Reason 'must not be null'")
                        .path("fileContents"));
    }

}