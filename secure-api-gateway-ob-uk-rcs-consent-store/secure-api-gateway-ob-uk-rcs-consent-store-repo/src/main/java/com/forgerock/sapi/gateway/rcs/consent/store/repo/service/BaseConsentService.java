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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.util.MultiValueMap;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.BaseConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;

public abstract class BaseConsentService<T extends BaseConsentEntity<?>, A extends AuthoriseConsentArgs> implements ConsentService<T, A> {

    private final Consumer<T> NO_VALIDATION_STRATEGY = ignored -> {};

    protected final MongoRepository<T, String> repo;

    /**
     * Map of the valid state transitions.
     *
     * The key is the current Status of the Consent, and the values are the valid Status values that the Consent
     * is allowed to transition to.
     */
    private final MultiValueMap<String, String> validStateTransitions;

    private final Supplier<String> idGenerator;

    /**
     * Initial status of the consent when it has been newly created
     */
    private final String initialConsentStatus;

    /**
     * Status when the Consent has been Authorised by the Resource Owner
     */
    private final String authorisedConsentStatus;
    /**
     * Status when the Resource Owner rejects the Consent
     */
    private final String rejectedConsentStatus;
    /**
     * Status when the Resource Owner revokes an Authorisation previously given for a Consent
     */
    private final String revokedConsentStatus;
    /**
     * Consumer that applies API version validation to consents retrieved from the repo. The Consumer should throw
     * a {@link ConsentStoreException} if the Consent should not be used with a particular API version.
     * <p>
     * The Consumer is wrapped in an AtomicReference to ensure thread safety with respect to customising this behaviour
     * when the service has been created.
     * <p>
     * By default, no validation is carried out.
     */
    private final AtomicReference<Consumer<T>> apiVersionValidationStrategy = new AtomicReference<>(NO_VALIDATION_STRATEGY);

    public BaseConsentService(MongoRepository<T, String> repo, Supplier<String> idGenerator, ConsentStateModel consentStateModel) {
        this.repo = requireNonNull(repo, "repo must be provided");
        this.idGenerator = requireNonNull(idGenerator, "idGenerator must be provided");

        requireNonNull(consentStateModel, "consentStateModel must be provided");
        this.validStateTransitions = consentStateModel.getValidStateTransitions();
        this.initialConsentStatus = consentStateModel.getInitialConsentStatus();
        this.authorisedConsentStatus = consentStateModel.getAuthorisedConsentStatus();
        this.rejectedConsentStatus = consentStateModel.getRejectedConsentStatus();
        this.revokedConsentStatus = consentStateModel.getRevokedConsentStatus();
    }

    void setApiVersionValidationStrategy(Consumer<T> apiVersionValidationStrategy) {
        requireNonNull(apiVersionValidationStrategy, "apiVersionValidationStrategy must be provided");
        this.apiVersionValidationStrategy.set(apiVersionValidationStrategy);
    }

    @Override
    public T createConsent(T consent) {
        if (consent.getId() != null && !consent.getId().startsWith("DVRP")) {
            throw new IllegalStateException("Cannot create consent, object already has an id: " + consent.getId());
        }

        if (consent.getId() == null) {
            consent.setId(idGenerator.get());
        }
        consent.setStatus(initialConsentStatus);

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
        if (consent.isDeleted()) {
            throw new ConsentStoreException(ErrorType.NOT_FOUND, consentId);
        }
        apiVersionValidationStrategy.get().accept(consent);
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

    protected void validateStateTransition(T consent, String targetStatus) {
        if (!isTransitionAllowed(consent, targetStatus)) {
            throw new ConsentStoreException(ErrorType.INVALID_STATE_TRANSITION, consent.getId(),
                    "cannot transition from consentStatus: " + consent.getStatus() + " to status: " + targetStatus);
        }
    }

    private boolean isTransitionAllowed(T consent, String targetStatus) {
        final List<String> validTransitions = validStateTransitions.get(consent.getStatus());
        return validTransitions != null && validTransitions.contains(targetStatus);
    }

    @Override
    public T rejectConsent(String consentId, String apiClientId, String resourceOwnerId) {
        final T consent = getConsent(consentId, apiClientId);
        validateStateTransition(consent, rejectedConsentStatus);
        consent.setStatus(rejectedConsentStatus);
        consent.setResourceOwnerId(resourceOwnerId);

        return repo.save(consent);
    }

    @Override
    public void deleteConsent(String consentId, String apiClientId) {
        final T consent = getConsent(consentId, apiClientId);
        consent.setStatus(revokedConsentStatus);
        consent.setDeleted(true);
        repo.save(consent);
    }

    @Override
    public void deleteConsentForMigration(String consentId, String apiClientId) {
        repo.deleteById(consentId);
    }

    @Override
    public boolean canTransitionToAuthorisedState(T consent) {
        return isTransitionAllowed(consent, authorisedConsentStatus);
    }
}
