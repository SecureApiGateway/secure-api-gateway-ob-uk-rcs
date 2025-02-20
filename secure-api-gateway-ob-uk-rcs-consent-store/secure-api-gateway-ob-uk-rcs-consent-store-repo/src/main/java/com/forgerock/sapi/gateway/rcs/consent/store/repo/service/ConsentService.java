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

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.BaseConsentEntity;

@Validated
public interface ConsentService<T extends BaseConsentEntity, A extends AuthoriseConsentArgs> {

    /**
     * Stores a new consent in the data store
     *
     * @param consent the consent to create
     * @return the persisted consent, with an id allocated
     */
    T createConsent(@Valid T consent);

    T getConsent(String consentId, String apiClientId);

    T authoriseConsent(@Valid A authoriseConsentArgs);

    T rejectConsent(String consentId, String apiClientId, String resourceOwnerId);

    void deleteConsent(String consentId, String apiClientId);

    /**
     * Can the Consent transition from its current state to Authorised state
     */
    boolean canTransitionToAuthorisedState(T consent);

}
