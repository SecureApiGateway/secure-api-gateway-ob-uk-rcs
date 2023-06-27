package com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.file;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteFileConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.BasePaymentConsentEntity;

/**
 * OBIE File Payment Consent: https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/pisp/file-payment-consents.html
 */
@Document("FilePaymentConsent")
@Validated
public class FilePaymentConsentEntity extends BasePaymentConsentEntity<FRWriteFileConsent> {

    private String fileContent;

    /**
     * IdempotencyKey for the file upload operation
     */
    private String fileUploadIdempotencyKey;

    /**
     * Time at which the use of the fileUploadIdempotencyKey expires, and the ApiClient is then able to reuse it with
     * a different file upload operation
     */
    private DateTime fileUploadIdempotencyKeyExpiration;

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileUploadIdempotencyKey() {
        return fileUploadIdempotencyKey;
    }

    public void setFileUploadIdempotencyKey(String fileUploadIdempotencyKey) {
        this.fileUploadIdempotencyKey = fileUploadIdempotencyKey;
    }

    public DateTime getFileUploadIdempotencyKeyExpiration() {
        return fileUploadIdempotencyKeyExpiration;
    }

    public void setFileUploadIdempotencyKeyExpiration(DateTime fileUploadIdempotencyKeyExpiration) {
        this.fileUploadIdempotencyKeyExpiration = fileUploadIdempotencyKeyExpiration;
    }
}
