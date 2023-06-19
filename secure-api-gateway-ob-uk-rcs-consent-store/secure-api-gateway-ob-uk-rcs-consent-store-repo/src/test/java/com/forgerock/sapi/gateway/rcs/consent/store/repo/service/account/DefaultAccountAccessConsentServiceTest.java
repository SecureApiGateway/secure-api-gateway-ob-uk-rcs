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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentServiceTest;
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
    protected BaseConsentService<AccountAccessConsentEntity, AccountAccessAuthoriseConsentArgs> getConsentServiceToTest() {
        return accountAccessConsentService;
    }

    @Override
    protected AccountAccessConsentEntity getValidConsentEntity() {
        final AccountAccessConsentEntity accountAccessConsentEntity = new AccountAccessConsentEntity();
        accountAccessConsentEntity.setApiClientId("test-api-client-1");
        accountAccessConsentEntity.setStatus(getNewConsentStatus());
        accountAccessConsentEntity.setRequestType(OBReadConsent1.class.getSimpleName());
        accountAccessConsentEntity.setRequestVersion(OBVersion.v3_1_10);

        final OBReadConsent1 obReadConsent = new OBReadConsent1();
        obReadConsent.setData(new OBReadData1().permissions(List.of(OBExternalPermissions1Code.READACCOUNTSBASIC)));
        obReadConsent.setRisk(new OBRisk2());
        accountAccessConsentEntity.setRequestObj(obReadConsent);

        return accountAccessConsentEntity;
    }

    @Override
    protected AccountAccessAuthoriseConsentArgs getAuthoriseConsentArgs(String consentId, String resourceOwnerId, String apiClientId) {
        return new AccountAccessAuthoriseConsentArgs(consentId, resourceOwnerId, apiClientId, List.of("acc-1", "acc-2"));
    }

    @Override
    protected String getNewConsentStatus() {
        return OBExternalRequestStatus1Code.AWAITINGAUTHORISATION.toString();
    }

    @Override
    protected String getAuthorisedConsentStatus() {
        return OBExternalRequestStatus1Code.AUTHORISED.toString();
    }

    @Override
    protected String getRejectedConsentStatus() {
        return OBExternalRequestStatus1Code.REJECTED.toString();
    }

    @Override
    protected void validateConsentSpecificFields(AccountAccessConsentEntity expected, AccountAccessConsentEntity actual) {
    }

    @Override
    protected void validateConsentSpecificAuthorisationFields(AccountAccessConsentEntity authorisedConsent, AccountAccessAuthoriseConsentArgs authorisationArgs) {
        assertThat(authorisationArgs.getAuthorisedAccountIds()).isNotNull().isNotEmpty();
        assertThat(authorisedConsent.getAuthorisedAccountIds()).isEqualTo(authorisationArgs.getAuthorisedAccountIds());
    }
}
