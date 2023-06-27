package com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.payment.file;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.file.FilePaymentConsentEntity;

@DataMongoTest
class FilePaymentConsentRepositoryTest {

    @Autowired
    private FilePaymentConsentRepository repo;

    @Test
    public void findConsentUsingIdempotencyData() {
        final String key1 = UUID.randomUUID().toString();
        final String apiClientId = "client-id-987";
        final Optional<FilePaymentConsentEntity> consent = repo.findByFileUploadIdempotencyData(apiClientId, key1, DateTime.now());
        assertFalse(consent.isPresent());

        final FilePaymentConsentEntity entity = new FilePaymentConsentEntity();
        entity.setFileUploadIdempotencyKey(key1);
        entity.setFileUploadIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        entity.setApiClientId(apiClientId);
        final FilePaymentConsentEntity savedEntity = repo.save(entity);

        final Optional<FilePaymentConsentEntity> idempotencyQueryResult = repo.findByFileUploadIdempotencyData(apiClientId, key1, DateTime.now());
        assertTrue(idempotencyQueryResult.isPresent());
        Assertions.assertThat(idempotencyQueryResult.get()).usingRecursiveComparison().isEqualTo(savedEntity);
    }

    @Test
    public void doesNotFindConsentForIdempotencyKeyUsedByDifferentApiClients() {
        final String key = UUID.randomUUID().toString();
        final String client1 = "client-id-987";
        final FilePaymentConsentEntity entity = new FilePaymentConsentEntity();
        entity.setFileUploadIdempotencyKey(key);
        entity.setFileUploadIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        entity.setApiClientId(client1);

        repo.save(entity);

        assertFalse(repo.findByFileUploadIdempotencyData("client-2", key, DateTime.now()).isPresent());
    }

    @Test
    public void doesNotFindConsentForExpiredIdempotencyKey() {
        final DateTime now = DateTime.now();
        final DateTime[] expiredTimes = new DateTime[] {
                now,
                now.minusMillis(1),
                now.minusMonths(1)
        };

        for (DateTime expiredTime : expiredTimes) {
            final String key = UUID.randomUUID().toString();
            final String apiClient = "client-id-987";
            final FilePaymentConsentEntity entity = new FilePaymentConsentEntity();
            entity.setFileUploadIdempotencyKey(key);
            entity.setFileUploadIdempotencyKeyExpiration(expiredTime);
            entity.setApiClientId(apiClient);

            repo.save(entity);
            assertFalse(repo.findByFileUploadIdempotencyData(apiClient, key, now).isPresent());
        }
    }
}