package com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.payment.file;

import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.Query;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.file.FilePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.payment.PaymentConsentRepository;

public interface FilePaymentConsentRepository  extends PaymentConsentRepository<FilePaymentConsentEntity> {

    @Query("{ 'apiClientId': ?0, 'fileUploadIdempotencyKey' : ?1, 'fileUploadIdempotencyKeyExpiration': {$gt: ?2 } }")
    Optional<FilePaymentConsentEntity> findByFileUploadIdempotencyData(String apiClientId, String fileUploadIdempotencyKey, DateTime currentTime);

}
