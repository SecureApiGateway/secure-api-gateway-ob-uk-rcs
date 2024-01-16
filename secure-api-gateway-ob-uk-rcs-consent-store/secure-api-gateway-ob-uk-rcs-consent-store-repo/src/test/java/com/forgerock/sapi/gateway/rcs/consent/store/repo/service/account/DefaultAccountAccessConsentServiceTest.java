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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentStateModel;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadConsent1;
import uk.org.openbanking.datamodel.account.OBReadData1;
import uk.org.openbanking.datamodel.account.OBRisk2;
import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DefaultAccountAccessConsentServiceTest extends BaseConsentServiceTest<AccountAccessConsentEntity, AccountAccessAuthoriseConsentArgs> {

    @Autowired
    private DefaultAccountAccessConsentService accountAccessConsentService;

    @Override
    protected ConsentStateModel getConsentStateModel() {
        return AccountAccessConsentStateModel.getInstance();
    }

    @Override
    protected BaseConsentService<AccountAccessConsentEntity, AccountAccessAuthoriseConsentArgs> getConsentServiceToTest() {
        return accountAccessConsentService;
    }

    @Override
    protected AccountAccessConsentEntity getValidConsentEntity() {
        return createValidConsentEntity("test-api-client-1");
    }

    public static AccountAccessConsentEntity createValidConsentEntity(String apiClientId) {
        final AccountAccessConsentEntity accountAccessConsentEntity = new AccountAccessConsentEntity();
        accountAccessConsentEntity.setApiClientId(apiClientId);
        accountAccessConsentEntity.setStatus(OBExternalRequestStatus1Code.AWAITINGAUTHORISATION.toString());
        accountAccessConsentEntity.setRequestVersion(OBVersion.v3_1_10);

        final OBReadConsent1 obReadConsent = new OBReadConsent1();
        obReadConsent.setData(new OBReadData1().permissions(List.of(OBExternalPermissions1Code.READACCOUNTSBASIC))
                                               .expirationDateTime(DateTime.now().plusDays(30)));
        obReadConsent.setRisk(new OBRisk2());
        accountAccessConsentEntity.setRequestObj(FRReadConsentConverter.toFRReadConsent(obReadConsent));

        return accountAccessConsentEntity;
    }

    @Override
    protected AccountAccessAuthoriseConsentArgs getAuthoriseConsentArgs(String consentId, String resourceOwnerId, String apiClientId) {
        return new AccountAccessAuthoriseConsentArgs(consentId, apiClientId, resourceOwnerId, List.of("acc-1", "acc-2"));
    }

    @Override
    protected void validateConsentSpecificFields(AccountAccessConsentEntity expected, AccountAccessConsentEntity actual) {
    }

    @Override
    protected void validateConsentSpecificAuthorisationFields(AccountAccessConsentEntity authorisedConsent, AccountAccessAuthoriseConsentArgs authorisationArgs) {
        assertThat(authorisationArgs.getAuthorisedAccountIds()).isNotNull().isNotEmpty();
        assertThat(authorisedConsent.getAuthorisedAccountIds()).isEqualTo(authorisationArgs.getAuthorisedAccountIds());
    }

    @Test
    void failToAuthoriseConsentNullOrEmptyAccountIds() {
        final AccountAccessConsentEntity persistedConsent = consentService.createConsent(getValidConsentEntity());
        final ConstraintViolationException ex = Assertions.assertThrows(ConstraintViolationException.class,
                () -> consentService.authoriseConsent(new AccountAccessAuthoriseConsentArgs(persistedConsent.getId(), persistedConsent.getApiClientId(), "user-123", null)));
        assertThat(ex.getConstraintViolations().stream().map(ConstraintViolation::getPropertyPath).map(Path::toString).collect(Collectors.toSet())).isEqualTo(Set.of("authoriseConsent.arg0.authorisedAccountIds"));
        assertThat(ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet())).isEqualTo(Set.of("must not be null", "must not be empty"));

        final ConstraintViolationException ex2 = Assertions.assertThrows(ConstraintViolationException.class,
                () -> consentService.authoriseConsent(new AccountAccessAuthoriseConsentArgs(persistedConsent.getId(), persistedConsent.getApiClientId(), "user-123", List.of())));
        assertThat(ex2.getMessage()).isEqualTo("authoriseConsent.arg0.authorisedAccountIds: must not be empty");
    }

    @Test
    void deleteConsent() {
        final AccountAccessConsentEntity consentObj = getValidConsentEntity();

        final AccountAccessConsentEntity persistedConsent = consentService.createConsent(consentObj);

        final String consentId = persistedConsent.getId();
        final AccountAccessAuthoriseConsentArgs authoriseConsentArgs = getAuthoriseConsentArgs(consentId, TEST_RESOURCE_OWNER, consentObj.getApiClientId());
        consentService.authoriseConsent(authoriseConsentArgs);

        consentService.deleteConsent(consentId, consentObj.getApiClientId());
        final ConsentStoreException consentStoreException = Assertions.assertThrows(ConsentStoreException.class, () -> consentService.getConsent(consentId, consentObj.getApiClientId()));
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    void testConsentCanBeReAuthenticated() {
        final AccountAccessConsentEntity consent = createValidConsentEntity("client-id");
        consent.setStatus(getConsentStateModel().getAuthorisedConsentStatus());
        assertThat(consentService.canTransitionToAuthorisedState(consent)).isTrue();
    }
}
