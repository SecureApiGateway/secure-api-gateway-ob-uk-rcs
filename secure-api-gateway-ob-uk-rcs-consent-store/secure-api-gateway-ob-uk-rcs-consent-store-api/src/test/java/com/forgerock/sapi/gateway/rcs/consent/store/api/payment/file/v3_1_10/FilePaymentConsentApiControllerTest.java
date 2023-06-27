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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.file.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.ApiTestUtils.createConsentStoreApiRequiredHeaders;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteFileConsentConverter;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.file.v3_1_10.CreateFilePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.file.v3_1_10.FilePaymentConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.file.v3_1_10.FileUploadRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;

import uk.org.openbanking.datamodel.payment.OBWriteFileConsent3;
import uk.org.openbanking.testsupport.payment.OBWriteFileConsentTestDataFactory;


public class FilePaymentConsentApiControllerTest extends BasePaymentConsentApiControllerTest<FilePaymentConsent, CreateFilePaymentConsentRequest> {

    public FilePaymentConsentApiControllerTest() {
        super(FilePaymentConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "file-payment-consents";
    }

    @Override
    protected CreateFilePaymentConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateFilePaymentConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateFilePaymentConsentRequest buildCreateFilePaymentConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateFilePaymentConsentRequest createFilePaymentConsentRequest = new CreateFilePaymentConsentRequest();
        final OBWriteFileConsent3 paymentConsent = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3("xml", "hash", "231", BigDecimal.ONE);
        createFilePaymentConsentRequest.setConsentRequest(FRWriteFileConsentConverter.toFRWriteFileConsent(paymentConsent));
        createFilePaymentConsentRequest.setApiClientId(apiClientId);
        createFilePaymentConsentRequest.setIdempotencyKey(idempotencyKey);
        return createFilePaymentConsentRequest;
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
        final FilePaymentConsent consent = createConsent(apiClientId);
        final ResponseEntity<FilePaymentConsent> fileUploadResponse = uploadFile(getValidFileUploadRequest(consent), FilePaymentConsent.class);
        assertThat(fileUploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        return fileUploadResponse.getBody();
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
        final FilePaymentConsent consent = createConsent(TEST_API_CLIENT_1);
        final FileUploadRequest validFileUploadRequest = getValidFileUploadRequest(consent);
        final ResponseEntity<FilePaymentConsent> fileUploadResponse = uploadFile(validFileUploadRequest, FilePaymentConsent.class);
        assertThat(fileUploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        final FilePaymentConsent consentWithFile = fileUploadResponse.getBody();

        FilePaymentConsentValidationHelpers.validateConsentAgainstFileUploadRequest(consentWithFile, validFileUploadRequest, consent);
    }

}