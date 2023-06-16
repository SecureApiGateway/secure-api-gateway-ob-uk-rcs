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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.BaseConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;

public abstract class BaseConsentService<T extends BaseConsentEntity<?>, A extends AuthoriseConsentArgs<T>> implements ConsentService<T, A> {

    protected final MongoRepository<T, String> repo;

    private final Supplier<String> idGenerator;
    private final String authorisedConsentStatus;
    private final String rejectedConsentStatus;
    private final String revokedConsentStatus;

    public BaseConsentService(MongoRepository<T, String> repo, Supplier<String> idGenerator, String authorisedConsentStatus,
                              String rejectedConsentStatus, String revokedConsentStatus) {

        this.repo = Objects.requireNonNull(repo, "repo must be provided");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator must be provided");
        this.authorisedConsentStatus = Objects.requireNonNull(authorisedConsentStatus, "authorisedConsentStatus must be provided");
        this.rejectedConsentStatus = Objects.requireNonNull(rejectedConsentStatus, "rejectedConsentStatus must be provided");
        this.revokedConsentStatus = Objects.requireNonNull(revokedConsentStatus, "revokedConsentStatus must be provided");
    }

    @Override
    public T createConsent(T consent) {
        if (consent.getId() != null) {
            throw new IllegalStateException("Cannot create consent, object already has an id: " + consent.getId());
        }
        consent.setId(idGenerator.get());

        return repo.insert(consent);
    }

    @Override
    public T getConsent(String consentId, String apiClientId) {
        final Optional<T> findResult = repo.findById(consentId);
        if (findResult.isEmpty()) {
            throw new ConsentStoreException(ErrorType.NOT_FOUND, consentId);
        }
        final T consent = findResult.get();
        if (!Objects.equals(consent.getApiClientId(), apiClientId)) {
            throw new ConsentStoreException(ErrorType.INVALID_PERMISSIONS, consentId);
        }
        return consent;
    }


    @Override
    public T authoriseConsent(A authoriseConsentArgs) {
        final T consent = getConsent(authoriseConsentArgs.getConsentId(), authoriseConsentArgs.getApiClientId());
        validateStateTransition(consent, authorisedConsentStatus);

        consent.setStatus(authorisedConsentStatus);
        consent.setResourceOwnerId(authoriseConsentArgs.getResourceOwnerId());
        addConsentSpecificAuthorisationData(consent, authoriseConsentArgs);

        return repo.save(consent);
    }

    protected abstract void addConsentSpecificAuthorisationData(T consent, A authoriseConsentArgs);

    protected abstract boolean isStateTransitionAllowed(String currentStatus, String targetStatus);

    protected void validateStateTransition(T consent, String targetStatus) {
        if (!isStateTransitionAllowed(consent.getStatus(), targetStatus)) {
            throw new ConsentStoreException(ErrorType.INVALID_STATE_TRANSITION, consent.getId(),
                    "cannot transition from consentStatus: " + consent.getStatus() + " to status: " + targetStatus);
        }
    }

    @Override
    public T rejectConsent(String consentId, String apiClientId, String resourceOwnerId) {
        final T consent = getConsent(consentId, apiClientId);
        consent.setStatus(rejectedConsentStatus);
        consent.setResourceOwnerId(resourceOwnerId);

        return repo.save(consent);
    }

    @Override
    public T revokeConsent(String consentId, String apiClientId) {
        final T consent = getConsent(consentId, consentId);
        consent.setStatus(revokedConsentStatus);
        return repo.save(consent);
    }
}
