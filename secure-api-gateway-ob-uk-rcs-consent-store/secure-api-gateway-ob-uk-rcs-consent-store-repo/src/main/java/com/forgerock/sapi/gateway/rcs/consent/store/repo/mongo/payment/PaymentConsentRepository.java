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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.payment;

import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.BasePaymentConsentEntity;

public interface PaymentConsentRepository<T extends BasePaymentConsentEntity> extends MongoRepository<T, String> {

    @Query("{ 'apiClientId': ?0, 'idempotencyKey' : ?1, 'idempotencyKeyExpiration': {$gt: ?2 } }")
    Optional<T> findByIdempotencyData(String apiClientId, String idempotencyKey, DateTime currentTime);

}
