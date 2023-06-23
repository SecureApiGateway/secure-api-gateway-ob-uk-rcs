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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.DomesticPaymentConsentEntity;

@DataMongoTest
class DomesticPaymentConsentRepositoryTest {

    @Autowired
    private DomesticPaymentConsentRepository repo;

    @Test
    public void findConsentUsingIdempotencyData() {
        final String key1 = UUID.randomUUID().toString();
        final String apiClientId = "client-id-987";
        final Optional<DomesticPaymentConsentEntity> consent = repo.findByIdempotencyData(apiClientId, key1, DateTime.now());
        assertFalse(consent.isPresent());

        final DomesticPaymentConsentEntity entity = new DomesticPaymentConsentEntity();
        entity.setIdempotencyKey(key1);
        entity.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        entity.setApiClientId(apiClientId);
        final DomesticPaymentConsentEntity savedEntity = repo.save(entity);

        final Optional<DomesticPaymentConsentEntity> idempotencyQueryResult = repo.findByIdempotencyData(apiClientId, key1, DateTime.now());
        assertTrue(idempotencyQueryResult.isPresent());
        Assertions.assertThat(idempotencyQueryResult.get()).usingRecursiveComparison().isEqualTo(savedEntity);
    }

    @Test
    public void doesNotFindConsentForIdempotencyKeyUsedByDifferentApiClients() {
        final String key = UUID.randomUUID().toString();
        final String client1 = "client-id-987";
        final DomesticPaymentConsentEntity entity = new DomesticPaymentConsentEntity();
        entity.setIdempotencyKey(key);
        entity.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        entity.setApiClientId(client1);

        repo.save(entity);

        assertFalse(repo.findByIdempotencyData("client-2", key, DateTime.now()).isPresent());
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
            final DomesticPaymentConsentEntity entity = new DomesticPaymentConsentEntity();
            entity.setIdempotencyKey(key);
            entity.setIdempotencyKeyExpiration(expiredTime);
            entity.setApiClientId(apiClient);

            repo.save(entity);
            assertFalse(repo.findByIdempotencyData(apiClient, key, now).isPresent());
        }
    }

}