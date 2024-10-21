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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.joda.time.DateTime;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.CreateFilePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FilePaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FileUploadRequest;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.payment.OBWriteFileConsentResponse4DataStatus;


public class FilePaymentConsentValidationHelpers {

    public static void validateCreateConsentAgainstCreateRequest(FilePaymentConsent consent,
                                                                 CreateFilePaymentConsentRequest createConsentRequest,
                                                                 OBVersion expectedVersion) {
        assertThat(consent.getId()).isNotEmpty();
        assertThat(consent.getStatus()).isEqualTo(OBWriteFileConsentResponse4DataStatus.AWAITINGUPLOAD.toString());
        assertThat(consent.getApiClientId()).isEqualTo(createConsentRequest.getApiClientId());
        assertThat(consent.getRequestObj()).isEqualTo(createConsentRequest.getConsentRequest());
        assertThat(consent.getRequestVersion()).isEqualTo(expectedVersion);
        assertThat(consent.getCharges()).isEqualTo(createConsentRequest.getCharges());
        assertThat(consent.getIdempotencyKey()).isEqualTo(createConsentRequest.getIdempotencyKey());
        assertThat(consent.getResourceOwnerId()).isNull();
        assertThat(consent.getAuthorisedDebtorAccountId()).isNull();
        assertThat(consent.getFileContent()).isNull();
        assertThat(consent.getFileUploadIdempotencyKey()).isNull();

        assertThat(consent.getIdempotencyKeyExpiration()).isGreaterThan(DateTime.now());
        assertThat(consent.getCreationDateTime()).isBefore(new Date());
        assertThat(consent.getStatusUpdateDateTime()).isEqualTo(consent.getCreationDateTime());
    }

    public static void validateConsentAgainstFileUploadRequest(FilePaymentConsent consentWithFile, FileUploadRequest fileUploadRequest, FilePaymentConsent originalConsent) {
        assertThat(consentWithFile.getStatus()).isEqualTo(OBWriteFileConsentResponse4DataStatus.AWAITINGAUTHORISATION.toString());
        assertThat(consentWithFile.getFileContent()).isEqualTo(fileUploadRequest.getFileContents());
        assertThat(consentWithFile.getFileUploadIdempotencyKey()).isEqualTo(fileUploadRequest.getFileUploadIdempotencyKey());

        assertThat(consentWithFile.getId()).isEqualTo(originalConsent.getId());
        assertThat(consentWithFile.getApiClientId()).isEqualTo(originalConsent.getApiClientId());
        assertThat(consentWithFile.getRequestObj()).isEqualTo(originalConsent.getRequestObj());
        assertThat(consentWithFile.getRequestVersion()).isEqualTo(originalConsent.getRequestVersion());
        assertThat(consentWithFile.getCreationDateTime()).isEqualTo(originalConsent.getCreationDateTime());
        assertThat(consentWithFile.getStatusUpdateDateTime()).isAfterOrEqualTo(originalConsent.getStatusUpdateDateTime()).isBeforeOrEqualTo(new Date());
    }

    public static void validateAuthorisedConsent(FilePaymentConsent authorisedConsent, AuthorisePaymentConsentRequest authoriseReq, FilePaymentConsent originalConsent) {
        assertThat(authorisedConsent.getStatus()).isEqualTo(OBWriteFileConsentResponse4DataStatus.AUTHORISED.toString());
        assertThat(authorisedConsent.getResourceOwnerId()).isEqualTo(authoriseReq.getResourceOwnerId());
        assertThat(authorisedConsent.getAuthorisedDebtorAccountId()).isEqualTo(authoriseReq.getAuthorisedDebtorAccountId());
        validateUpdatedConsentAgainstOriginal(authorisedConsent, originalConsent);
    }

    public static void validateRejectedConsent(FilePaymentConsent rejectedConsent, RejectConsentRequest rejectReq, FilePaymentConsent originalConsent) {
        assertThat(rejectedConsent.getStatus()).isEqualTo(OBWriteFileConsentResponse4DataStatus.REJECTED.toString());
        assertThat(rejectedConsent.getResourceOwnerId()).isEqualTo(rejectReq.getResourceOwnerId());
        validateUpdatedConsentAgainstOriginal(rejectedConsent, originalConsent);
    }

    /**
     * Validates fields in an updatedConsent vs an original consent.
     *
     * This checks that fields that should never change when a consent is updated do never change, and verifies that
     * the statusUpdateDateTime increases vs the original.
     */
    public static void validateUpdatedConsentAgainstOriginal(FilePaymentConsent updatedConsent, FilePaymentConsent consent) {
        assertThat(updatedConsent.getId()).isEqualTo(consent.getId());
        assertThat(updatedConsent.getApiClientId()).isEqualTo(consent.getApiClientId());
        assertThat(updatedConsent.getRequestObj()).isEqualTo(consent.getRequestObj());
        assertThat(updatedConsent.getRequestVersion()).isEqualTo(consent.getRequestVersion());
        assertThat(updatedConsent.getCreationDateTime()).isEqualTo(consent.getCreationDateTime());
        assertThat(updatedConsent.getStatusUpdateDateTime()).isBeforeOrEqualTo(new Date()).isAfterOrEqualTo(consent.getStatusUpdateDateTime());
        assertThat(updatedConsent.getFileUploadIdempotencyKey()).isEqualTo(consent.getFileUploadIdempotencyKey());
        assertThat(updatedConsent.getFileContent()).isEqualTo(consent.getFileContent());

    }

    /**
     * Validates fields of a consumedConsent against an authorisedConsent (the previous state)
     *
     * Checks that data added to the consent during authorisation is present in the consumedConsent
     */
    public static void validateConsumedConsent(FilePaymentConsent consumedConsent, FilePaymentConsent authorisedConsent) {
        assertThat(consumedConsent.getStatus()).isEqualTo(OBWriteFileConsentResponse4DataStatus.CONSUMED.toString());
        validateUpdatedConsentAgainstOriginal(consumedConsent, authorisedConsent);

        assertThat(consumedConsent.getStatusUpdateDateTime()).isAfterOrEqualTo(authorisedConsent.getStatusUpdateDateTime());
        assertThat(consumedConsent.getAuthorisedDebtorAccountId()).isEqualTo(authorisedConsent.getAuthorisedDebtorAccountId());
        assertThat(consumedConsent.getResourceOwnerId()).isEqualTo(authorisedConsent.getResourceOwnerId());
    }
}
