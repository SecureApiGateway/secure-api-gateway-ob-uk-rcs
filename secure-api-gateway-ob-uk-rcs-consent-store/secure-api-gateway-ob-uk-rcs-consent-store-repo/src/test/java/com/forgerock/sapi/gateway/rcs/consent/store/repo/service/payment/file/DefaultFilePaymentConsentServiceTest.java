/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.AssertionsForClassTypes;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRChargeBearerType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRWriteFileConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.file.FilePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.BasePaymentConsentServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import jakarta.validation.ConstraintViolationException;
import uk.org.openbanking.datamodel.v3.payment.OBWriteFileConsent3;
import uk.org.openbanking.datamodel.v3.payment.OBWriteFileConsentResponse4DataStatus;
import uk.org.openbanking.testsupport.v3.payment.OBWriteFileConsentTestDataFactory;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DefaultFilePaymentConsentServiceTest extends BasePaymentConsentServiceTest<FilePaymentConsentEntity> {

    @Autowired
    private DefaultFilePaymentConsentService service;

    @Override
    protected ConsentStateModel getConsentStateModel() {
        return FilePaymentConsentStateModel.getInstance();
    }

    @Override
    protected BaseConsentService<FilePaymentConsentEntity, PaymentAuthoriseConsentArgs> getConsentServiceToTest() {
        return service;
    }

    @Override
    protected FilePaymentConsentEntity getConsentInStateToAuthoriseOrReject() {
        final FilePaymentConsentEntity consentObj = getValidConsentEntity();
        final FilePaymentConsentEntity initialConsent = consentService.createConsent(consentObj);
        return service.uploadFile(createValidFileUploadArgs(initialConsent));
    }

    @Override
    protected FilePaymentConsentEntity getValidConsentEntity() {
        final String apiClientId = "test-client-987";
        return createValidConsentEntity(apiClientId);
    }

    public static FilePaymentConsentEntity createValidConsentEntity(String apiClientId) {
        return createValidConsentEntity(OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3(
                "test-type", "hash12343", "3", BigDecimal.ONE), apiClientId);
    }

    public static FilePaymentConsentEntity createValidConsentEntity(OBWriteFileConsent3 obConsent, String apiClientId) {
        final FilePaymentConsentEntity domesticPaymentConsent = new FilePaymentConsentEntity();
        domesticPaymentConsent.setRequestVersion(OBVersion.v3_1_10);
        domesticPaymentConsent.setApiClientId(apiClientId);
        domesticPaymentConsent.setRequestObj(FRWriteFileConsentConverter.toFRWriteFileConsent(obConsent));
        domesticPaymentConsent.setStatus(OBWriteFileConsentResponse4DataStatus.AWAITINGUPLOAD.toString());
        domesticPaymentConsent.setIdempotencyKey(UUID.randomUUID().toString());
        domesticPaymentConsent.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        domesticPaymentConsent.setCharges(List.of(
                FRCharge.builder().type("fee1")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.15","GBP"))
                        .build(),
                FRCharge.builder().type("fee2")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.10","GBP"))
                        .build())
        );
        return domesticPaymentConsent;
    }

    @Test
    void uploadFile() {
        final FilePaymentConsentEntity consentObj = getValidConsentEntity();
        final FilePaymentConsentEntity persistedConsent = service.createConsent(consentObj);

        final FileUploadArgs fileUploadArgs = createValidFileUploadArgs(persistedConsent);
        final FilePaymentConsentEntity consentWithFile = service.uploadFile(fileUploadArgs);

        assertThat(consentWithFile.getStatus()).isEqualTo(OBWriteFileConsentResponse4DataStatus.AWAITINGAUTHORISATION.toString());
        assertThat(consentWithFile.getFileContent()).isEqualTo(fileUploadArgs.getFileContents());
        assertThat(consentWithFile.getFileUploadIdempotencyKey()).isEqualTo(fileUploadArgs.getFileUploadIdempotencyKey());
        assertThat(consentWithFile.getId()).isEqualTo(persistedConsent.getId());
        assertThat(consentWithFile.getCharges()).isEqualTo(persistedConsent.getCharges());
        assertThat(consentWithFile.getApiClientId()).isEqualTo(persistedConsent.getApiClientId());
        assertThat(consentWithFile.getCreationDateTime()).isEqualTo(persistedConsent.getCreationDateTime());
        assertThat(consentWithFile.getStatusUpdatedDateTime()).isAfter(persistedConsent.getStatusUpdatedDateTime()).isBeforeOrEqualTo(new Date());
        assertThat(consentWithFile.getAuthorisedDebtorAccountId()).isNull();
        assertThat(consentWithFile.getResourceOwnerId()).isNull();
        assertThat(consentWithFile.getIdempotencyKey()).isEqualTo(persistedConsent.getIdempotencyKey());
        assertThat(consentWithFile.getIdempotencyKeyExpiration()).isEqualTo(persistedConsent.getIdempotencyKeyExpiration());
    }

    @Test
    void failToUploadFileIfFileMissing() {
        final FilePaymentConsentEntity consentObj = getValidConsentEntity();
        final FilePaymentConsentEntity persistedConsent = service.createConsent(consentObj);

        final FileUploadArgs fileUploadArgs = createValidFileUploadArgs(persistedConsent);
        fileUploadArgs.setFileContents(null);

        final ConstraintViolationException constraintViolationException = assertThrows(ConstraintViolationException.class, () -> service.uploadFile(fileUploadArgs));
        assertThat(constraintViolationException.getConstraintViolations()).hasSize(1);
        assertThat(constraintViolationException.getMessage()).isEqualTo("uploadFile.arg0.fileContents: must not be null");
    }

    @Test
    void uploadFileShouldBeIdempotent() {
        final FilePaymentConsentEntity persistedConsent = service.createConsent(getValidConsentEntity());

        final String fileUploadIdempotencyKey = "key-1";

        FilePaymentConsentEntity firstUploadResponse = null;
        for (int i = 0 ; i < 10; i++) {
            final FileUploadArgs validFileUploadArgs = createValidFileUploadArgs(persistedConsent);
            validFileUploadArgs.setFileUploadIdempotencyKey(fileUploadIdempotencyKey);
            final FilePaymentConsentEntity uploadFileResponse = service.uploadFile(validFileUploadArgs);
            if (firstUploadResponse == null) {
                firstUploadResponse = uploadFileResponse;
            } else {
                AssertionsForClassTypes.assertThat(uploadFileResponse).usingRecursiveComparison().isEqualTo(firstUploadResponse);
            }
        }
    }

    @Test
    void idempotentFileUploadShouldFailIfIdempotencyKeyChanged() {
        final FilePaymentConsentEntity persistedConsent = service.createConsent(getValidConsentEntity());
        final FileUploadArgs validFileUploadArgs = createValidFileUploadArgs(persistedConsent);
        service.uploadFile(validFileUploadArgs);

        validFileUploadArgs.setFileUploadIdempotencyKey(UUID.randomUUID().toString());
        final ConsentStoreException consentStoreException = assertThrows(ConsentStoreException.class, () -> service.uploadFile(validFileUploadArgs));
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.IDEMPOTENCY_ERROR);
    }

    @Test
    void idempotentFileUploadShouldFailIfFileContentsChanged() {
        final FilePaymentConsentEntity persistedConsent = service.createConsent(getValidConsentEntity());
        final FileUploadArgs validFileUploadArgs = createValidFileUploadArgs(persistedConsent);
        service.uploadFile(validFileUploadArgs);

        validFileUploadArgs.setFileContents(validFileUploadArgs.getFileContents() + "sdfsfsfd");
        final ConsentStoreException consentStoreException = assertThrows(ConsentStoreException.class, () -> service.uploadFile(validFileUploadArgs));
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.IDEMPOTENCY_ERROR);
    }

    @Test
    void failToAuthoriseConsentAwaitingUpload() {
        final FilePaymentConsentEntity consentObj = getValidConsentEntity();
        final FilePaymentConsentEntity persistedConsent = service.createConsent(consentObj);

        final ConsentStoreException consentStoreException = assertThrows(ConsentStoreException.class,
                () -> service.authoriseConsent(getAuthoriseConsentArgs(persistedConsent.getId(), TEST_RESOURCE_OWNER, persistedConsent.getApiClientId())));
        assertThat(consentStoreException.getConsentId()).isEqualTo(persistedConsent.getId());
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.INVALID_STATE_TRANSITION);
        assertThat(consentStoreException.getMessage()).contains("cannot transition from consentStatus: AwaitingUpload to status: Authorised");
    }

    @Test
    protected void testCanConsentBeAuthorised() {
        final FilePaymentConsentEntity consent = getValidConsentEntity();
        assertThat(consentService.canTransitionToAuthorisedState(consent)).isFalse();

        consent.setStatus(FilePaymentConsentStateModel.AWAITING_AUTHORISATION);
        assertThat(consentService.canTransitionToAuthorisedState(consent)).isTrue();

        consent.setStatus(getConsentStateModel().getRevokedConsentStatus());
        assertThat(consentService.canTransitionToAuthorisedState(consent)).isFalse();
    }



    private static FileUploadArgs createValidFileUploadArgs(FilePaymentConsentEntity persistedConsent) {
        final FileUploadArgs fileUploadArgs = new FileUploadArgs();
        fileUploadArgs.setFileContents("<xml>sdffsddf</xml>");
        fileUploadArgs.setConsentId(persistedConsent.getId());
        fileUploadArgs.setApiClientId(persistedConsent.getApiClientId());
        fileUploadArgs.setFileUploadIdempotencyKey(UUID.randomUUID().toString());
        return fileUploadArgs;
    }

}