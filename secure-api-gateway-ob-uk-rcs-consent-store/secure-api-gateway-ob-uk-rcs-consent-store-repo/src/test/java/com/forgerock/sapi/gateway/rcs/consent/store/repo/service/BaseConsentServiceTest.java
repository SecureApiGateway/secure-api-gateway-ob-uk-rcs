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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.BaseConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import jakarta.validation.ConstraintViolationException;

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

        final Date timeBeforePersist = new Date();
        final T persistedConsent = consentService.createConsent(consentObj);

        assertThat(persistedConsent.getId()).isNotBlank();
        assertThat(persistedConsent.getCreationDateTime()).isNotNull().isBetween(timeBeforePersist, new Date());
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

        final ConstraintViolationException constraintViolationException = assertThrows(ConstraintViolationException.class,
                () -> consentService.createConsent(consentObj));
        assertThat(constraintViolationException.getConstraintViolations()).hasSize(1);
        assertThat(constraintViolationException.getMessage()).isEqualTo("createConsent.arg0.apiClientId: must not be null");
    }

    @Test
    void failToGetConsentWhenApiClientIdDoesNotMatch() {
        final T persistedConsent = consentService.createConsent(getValidConsentEntity());

        final ConsentStoreException consentStoreException = assertThrows(ConsentStoreException.class, () -> consentService.getConsent(persistedConsent.getId(), "different-api-client-id"));
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.INVALID_PERMISSIONS);
    }

    @Test
    void failToGetIdThatDoesNotExist() {
        final ConsentStoreException consentStoreException = assertThrows(ConsentStoreException.class,
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
        assertThat(authorisedConsent.getStatusUpdatedDateTime()).isAfter(consentToAuthorise.getStatusUpdatedDateTime())
                                                                .isBeforeOrEqualTo(new Date());

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
        assertThat(rejectedConsent.getStatusUpdatedDateTime()).isAfterOrEqualTo(consentBeforeRejectAction.getStatusUpdatedDateTime())
                                                              .isBeforeOrEqualTo(new Date());
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

    @Test
    public void shouldAllowConsentVersionValidationToBeConfigured() {
        final MongoRepository mockRepo = mock(MongoRepository.class);
        final BaseConsentService<BaseConsentEntity<?>, AuthoriseConsentArgs> consentService = new BaseConsentService<>(mockRepo, () -> UUID.randomUUID().toString(), AccountAccessConsentStateModel.getInstance()) {
            @Override
            protected void addConsentSpecificAuthorisationData(BaseConsentEntity consent, AuthoriseConsentArgs authoriseConsentArgs) {
            }
        };

        final String v4ConsentId = "consent1";
        final String apiClientId = "client1";

        final AccountAccessConsentEntity repoConsentv4 = new AccountAccessConsentEntity();
        repoConsentv4.setId(v4ConsentId);
        repoConsentv4.setApiClientId(apiClientId);
        repoConsentv4.setRequestVersion(OBVersion.v4_0_0);
        when(mockRepo.findById(eq(v4ConsentId))).thenReturn(Optional.of(repoConsentv4));

        final String v319ConsentId = "consent2";
        final AccountAccessConsentEntity repoConsentv3 = new AccountAccessConsentEntity();
        repoConsentv3.setId(v319ConsentId);
        repoConsentv3.setApiClientId(apiClientId);
        repoConsentv3.setRequestVersion(OBVersion.v3_1_9);
        when(mockRepo.findById(eq(v319ConsentId))).thenReturn(Optional.of(repoConsentv3));

        // Sanity test fetching a consent with no version validation
        BaseConsentEntity<?> consent = consentService.getConsent(v4ConsentId, apiClientId);
        assertThat(consent).isEqualTo(repoConsentv4);

        consentService.setApiVersionValidationStrategy(consentToValidate -> {
            if (consentToValidate.getRequestVersion().equals(OBVersion.v3_1_9)) {
                throw new ConsentStoreException(ErrorType.INVALID_API_VERSION, "v3.1.9 not supported");
            }
        });

        // Fetch the v4 consent and ensure it passes the validation rule
        consent = consentService.getConsent(v4ConsentId, apiClientId);
        assertThat(consent).isEqualTo(repoConsentv4);

        // Fetch the v3 consent and ensure it fails the validation rule
        final ConsentStoreException ex = assertThrows(ConsentStoreException.class, () -> consentService.getConsent(v319ConsentId, apiClientId));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.INVALID_API_VERSION);
    }
}