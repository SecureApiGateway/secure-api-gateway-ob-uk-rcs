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

import static org.assertj.core.api.Assertions.assertThat;

import javax.validation.ConstraintViolationException;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.BaseConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;

public abstract class BaseConsentServiceTest<T extends BaseConsentEntity<?>, A extends AuthoriseConsentArgs> {

    protected static final String TEST_RESOURCE_OWNER = "test-user-1";

    protected BaseConsentService<T, A> consentService;

    @BeforeEach
    public void beforeEach() {
        consentService = getConsentServiceToTest();
    }

    protected abstract BaseConsentService<T, A> getConsentServiceToTest();

    protected abstract T getValidConsentEntity();

    protected abstract A getAuthoriseConsentArgs(String consentId, String resourceOwnerId, String apiClientId);

    protected abstract String getNewConsentStatus();

    protected abstract String getAuthorisedConsentStatus();

    protected abstract String getRejectedConsentStatus();

    protected abstract void validateConsentSpecificFields(T expected, T actual);

    protected abstract void validateConsentSpecificAuthorisationFields(T authorisedConsent, A authorisationArgs);


    @Test
    void createConsent() {
        final T consentObj = getValidConsentEntity();

        final DateTime timeBeforePersist = DateTime.now();
        final T persistedConsent = consentService.createConsent(consentObj);

        assertThat(persistedConsent.getId()).isNotBlank();
        assertThat(persistedConsent.getCreationDateTime()).isNotNull().isBetween(timeBeforePersist, DateTime.now());
        assertThat(persistedConsent.getStatusUpdatedDateTime()).isEqualTo(persistedConsent.getCreationDateTime());

        assertThat(persistedConsent.getStatus()).isEqualTo(getNewConsentStatus());

        assertThat(persistedConsent.getRequestVersion()).isEqualTo(consentObj.getRequestVersion());
        assertThat(persistedConsent.getApiClientId()).isEqualTo(consentObj.getApiClientId());

        // ResourceOwner not set on new consents
        assertThat(persistedConsent.getResourceOwnerId()).isNull();

        validateConsentSpecificFields(consentObj, persistedConsent);

        final T consentReturnedByGet = consentService.getConsent(persistedConsent.getId(), persistedConsent.getApiClientId());
        assertThat(persistedConsent).usingRecursiveComparison().isEqualTo(consentReturnedByGet);
    }

    @Test
    void failToCreateConsentMissingMandatoryFields() {
        final T consentObj = getValidConsentEntity();
        consentObj.setApiClientId(null); // remove mandatory field value

        final ConstraintViolationException constraintViolationException = Assertions.assertThrows(ConstraintViolationException.class,
                () -> consentService.createConsent(consentObj));
        assertThat(constraintViolationException.getConstraintViolations()).hasSize(1);
        assertThat(constraintViolationException.getMessage()).isEqualTo("createConsent.arg0.apiClientId: must not be null");
    }

    @Test
    void failToGetConsentWhenApiClientIdDoesNotMatch() {
        final T persistedConsent = consentService.createConsent(getValidConsentEntity());

        final ConsentStoreException consentStoreException = Assertions.assertThrows(ConsentStoreException.class, () -> consentService.getConsent(persistedConsent.getId(), "different-api-client-id"));
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.INVALID_PERMISSIONS);
    }

    @Test
    void failToGetIdThatDoesNotExist() {
        final ConsentStoreException consentStoreException = Assertions.assertThrows(ConsentStoreException.class,
                () -> getConsentServiceToTest().getConsent("does-not-exist", "client-1"));
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    void authoriseConsent() {
        final T consentObj = getValidConsentEntity();

        final T persistedConsent = consentService.createConsent(consentObj);

        final A authoriseConsentArgs = getAuthoriseConsentArgs(persistedConsent.getId(), TEST_RESOURCE_OWNER, persistedConsent.getApiClientId());
        final T authorisedConsent = consentService.authoriseConsent(authoriseConsentArgs);

        assertThat(authorisedConsent.getStatus()).isEqualTo(getAuthorisedConsentStatus());
        assertThat(authorisedConsent.getResourceOwnerId()).isEqualTo(TEST_RESOURCE_OWNER);
        assertThat(authorisedConsent.getStatusUpdatedDateTime()).isGreaterThan(persistedConsent.getStatusUpdatedDateTime())
                                                                .isLessThan(DateTime.now());

        assertThat(authorisedConsent.getCreationDateTime()).isEqualTo(persistedConsent.getCreationDateTime());
        assertThat(authorisedConsent.getApiClientId()).isEqualTo(persistedConsent.getApiClientId());
        assertThat(authorisedConsent.getId()).isEqualTo(persistedConsent.getId());
        assertThat(authorisedConsent.getRequestVersion()).isEqualTo(persistedConsent.getRequestVersion());
        assertThat(authorisedConsent.getRequestObj()).isEqualTo(persistedConsent.getRequestObj());
        validateConsentSpecificFields(persistedConsent, authorisedConsent);

        validateConsentSpecificAuthorisationFields(authorisedConsent, authoriseConsentArgs);
    }


    @Test
    void rejectConsent() {
        final T consentObj = getValidConsentEntity();

        final T persistedConsent = consentService.createConsent(consentObj);
        final T rejectedConsent = consentService.rejectConsent(persistedConsent.getId(), consentObj.getApiClientId(), TEST_RESOURCE_OWNER);

        validateRejectedConsent(persistedConsent, rejectedConsent);
    }

    protected void validateRejectedConsent(T consentBeforeRejectAction, T rejectedConsent) {
        final RecursiveComparisonConfiguration recursiveComparisonConfiguration = RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("status", "resourceOwnerId", "statusUpdatedDateTime", "entityVersion").build();

        assertThat(rejectedConsent).usingRecursiveComparison(recursiveComparisonConfiguration).isEqualTo(consentBeforeRejectAction);
        assertThat(rejectedConsent.getStatus()).isEqualTo(getRejectedConsentStatus());
        assertThat(rejectedConsent.getResourceOwnerId()).isEqualTo(TEST_RESOURCE_OWNER);

        assertThat(rejectedConsent.getStatusUpdatedDateTime()).isGreaterThan(consentBeforeRejectAction.getStatusUpdatedDateTime())
                .isLessThan(DateTime.now());
        assertThat(rejectedConsent.getEntityVersion()).isEqualTo(consentBeforeRejectAction.getEntityVersion() + 1);
    }
}