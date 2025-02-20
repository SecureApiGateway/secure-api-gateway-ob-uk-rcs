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

import java.util.function.Consumer;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.BaseConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.version.ApiVersionValidator;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

public abstract class ConsentServiceFactory<T extends BaseConsentEntity<?>, A extends AuthoriseConsentArgs, S extends BaseConsentService<T, A>> {

    protected final MongoRepository<T, String> repo;
    protected final ApiVersionValidator apiVersionValidator;

    protected ConsentServiceFactory(MongoRepository<T, String> repo, ApiVersionValidator apiVersionValidator) {
        this.repo = requireNonNull(repo, "repo cannot be null");
        this.apiVersionValidator = requireNonNull(apiVersionValidator, "apiVersionValidator cannot be null");
    }

    protected abstract S createBaseConsentService();

    /**
     * Creates a ConsentService to be used by the RCS internally, this service applies no API version validation
     * @return the ConsentService
     */
    public S createInternalConsentService() {
        return createBaseConsentService();
    }

    /**
     * Creates an API Consent Service, this service is tied to a particular API version and will validate consents
     * retrieved from the repository can be accessed by that version.
     *
     * @param apiVersion OBVersion being used to access the consent
     * @return the ConsentService
     */
    public S createApiConsentService(OBVersion apiVersion) {
        requireNonNull(apiVersion, "apiVersion cannot be null");
        final S baseConsentService = createBaseConsentService();
        baseConsentService.setApiVersionValidationStrategy(applyApiVersionValidator(apiVersion));
        return baseConsentService;
    }

    private Consumer<T> applyApiVersionValidator(OBVersion apiVersion) {
        return consent -> {
            if (!apiVersionValidator.canAccessResourceUsingApiVersion(consent.getRequestVersion(), apiVersion)) {
                throw new ConsentStoreException(ErrorType.INVALID_API_VERSION, consent.getId(),
                        "Consent created using API version: " + consent.getRequestVersion().getCanonicalVersion()
                                + " cannot be accessed using version: " + apiVersion.getCanonicalVersion());
            }
        };
    }

}
