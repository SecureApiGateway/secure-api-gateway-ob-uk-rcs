/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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

    protected abstract ConsentStateModel getConsentStateModel();

    protected abstract T getValidConsentEntity();

    protected abstract A getAuthoriseConsentArgs(String consentId, String resourceOwnerId, String apiClientId);

    protected abstract void validateConsentSpecificFields(T expected, T actual);

    protected abstract void validateConsentSpecificAuthorisationFields(T authorisedConsent, A authorisationArgs);

    protected T getConsentInStateToAuthoriseOrReject() {
        final T consentObj = getValidConsentEntity();
        return consentService.createConsent(consentObj);
    }

    @Test
    void createConsent() {
        final T consentObj = getValidConsentEntity();

        final DateTime timeBeforePersist = DateTime.now();
        final T persistedConsent = consentService.createConsent(consentObj);

        assertThat(persistedConsent.getId()).isNotBlank();
        assertThat(persistedConsent.getCreationDateTime()).isNotNull().isBetween(timeBeforePersist, DateTime.now());
        assertThat(persistedConsent.getStatusUpdatedDateTime()).isEqualTo(persistedConsent.getCreationDateTime());

        assertThat(persistedConsent.getStatus()).isEqualTo(getConsentStateModel().getInitialConsentStatus());

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
        final T consentToAuthorise = getConsentInStateToAuthoriseOrReject();

        final A authoriseConsentArgs = getAuthoriseConsentArgs(consentToAuthorise.getId(), TEST_RESOURCE_OWNER, consentToAuthorise.getApiClientId());
        final T authorisedConsent = consentService.authoriseConsent(authoriseConsentArgs);

        assertThat(authorisedConsent.getStatus()).isEqualTo(getConsentStateModel().getAuthorisedConsentStatus());
        assertThat(authorisedConsent.getResourceOwnerId()).isEqualTo(TEST_RESOURCE_OWNER);
        assertThat(authorisedConsent.getStatusUpdatedDateTime()).isGreaterThan(consentToAuthorise.getStatusUpdatedDateTime())
                                                                .isLessThanOrEqualTo(DateTime.now());

        assertThat(authorisedConsent.getCreationDateTime()).isEqualTo(consentToAuthorise.getCreationDateTime());
        assertThat(authorisedConsent.getApiClientId()).isEqualTo(consentToAuthorise.getApiClientId());
        assertThat(authorisedConsent.getId()).isEqualTo(consentToAuthorise.getId());
        assertThat(authorisedConsent.getRequestVersion()).isEqualTo(consentToAuthorise.getRequestVersion());
        assertThat(authorisedConsent.getRequestObj()).isEqualTo(consentToAuthorise.getRequestObj());
        validateConsentSpecificFields(consentToAuthorise, authorisedConsent);

        validateConsentSpecificAuthorisationFields(authorisedConsent, authoriseConsentArgs);
    }


    @Test
    void rejectConsent() {
        final T consentToReject = getConsentInStateToAuthoriseOrReject();
        final T rejectedConsent = consentService.rejectConsent(consentToReject.getId(), consentToReject.getApiClientId(), TEST_RESOURCE_OWNER);

        validateRejectedConsent(consentToReject, rejectedConsent);
    }

    protected void validateRejectedConsent(T consentBeforeRejectAction, T rejectedConsent) {
        final RecursiveComparisonConfiguration recursiveComparisonConfiguration = RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("status", "resourceOwnerId", "statusUpdatedDateTime", "entityVersion").build();

        assertThat(rejectedConsent).usingRecursiveComparison(recursiveComparisonConfiguration).isEqualTo(consentBeforeRejectAction);
        assertThat(rejectedConsent.getStatus()).isEqualTo(getConsentStateModel().getRejectedConsentStatus());
        assertThat(rejectedConsent.getResourceOwnerId()).isEqualTo(TEST_RESOURCE_OWNER);

        // sometimes actions complete so fast that statusUpdatedTime is the same (to millisecond precision)
        assertThat(rejectedConsent.getStatusUpdatedDateTime()).isGreaterThanOrEqualTo(consentBeforeRejectAction.getStatusUpdatedDateTime())
                                                              .isLessThanOrEqualTo(DateTime.now());
        assertThat(rejectedConsent.getEntityVersion()).isEqualTo(consentBeforeRejectAction.getEntityVersion() + 1);
    }

    @Test
    protected void testCanConsentBeAuthorised() {
        final T consent = getValidConsentEntity();
        assertThat(consentService.canTransitionToAuthorisedState(consent)).isTrue();

        consent.setStatus(getConsentStateModel().getRejectedConsentStatus());
        assertThat(consentService.canTransitionToAuthorisedState(consent)).isFalse();

        consent.setStatus(getConsentStateModel().getRevokedConsentStatus());
        assertThat(consentService.canTransitionToAuthorisedState(consent)).isFalse();
    }
}