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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.funds.FRFundsConfirmationConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.funds.FundsConfirmationConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentStateModel;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.joda.time.DateTime;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.org.openbanking.datamodel.common.OBCashAccount3;
import uk.org.openbanking.datamodel.common.OBExternalAccountIdentification2Code;
import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsent1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsentData1;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Test for {@link DefaultFundsConfirmationAccessConsentService}
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DefaultFundsConfirmationAccessConsentServiceTest extends BaseConsentServiceTest<FundsConfirmationConsentEntity, FundsConfirmationAuthoriseConsentArgs> {

    private static final String API_CLIENT_ID = UUID.randomUUID().toString();
    private static final String DEBTOR_AUTHORISED_ACCOUNT_ID = UUID.randomUUID().toString();
    @Autowired
    private DefaultFundsConfirmationAccessConsentService fundsConfirmationAccessConsentService;

    @Override
    protected BaseConsentService<FundsConfirmationConsentEntity, FundsConfirmationAuthoriseConsentArgs> getConsentServiceToTest() {
        return fundsConfirmationAccessConsentService;
    }

    @Override
    protected ConsentStateModel getConsentStateModel() {
        return FundsConfirmationConsentStateModel.getInstance();
    }

    @Override
    protected FundsConfirmationConsentEntity getValidConsentEntity() {
        return createValidConsentEntity(API_CLIENT_ID);
    }

    @Override
    protected FundsConfirmationAuthoriseConsentArgs getAuthoriseConsentArgs(String consentId, String resourceOwnerId, String apiClientId) {
        return new FundsConfirmationAuthoriseConsentArgs(consentId, apiClientId, resourceOwnerId, DEBTOR_AUTHORISED_ACCOUNT_ID);
    }

    @Override
    protected void validateConsentSpecificFields(FundsConfirmationConsentEntity expected, FundsConfirmationConsentEntity actual) {
    }

    @Override
    protected void validateConsentSpecificAuthorisationFields(FundsConfirmationConsentEntity authorisedConsent, FundsConfirmationAuthoriseConsentArgs authorisationArgs) {
        assertThat(authorisedConsent.getAuthorisedDebtorAccountId()).isEqualTo(authorisationArgs.getAuthorisedDebtorAccountId());
    }

    public static FundsConfirmationConsentEntity createValidConsentEntity(String apiClientId) {
        final FRAccountIdentifier accountIdentifier = FRAccountIdentifier.builder()
                .accountId(UUID.randomUUID().toString())
                .name("account-name")
                .schemeName(OBExternalAccountIdentification2Code.SortCodeAccountNumber.toString())
                .identification("08080021325698")
                .secondaryIdentification("secondary-identification")
                .build();
        return createValidConsentEntity(apiClientId, accountIdentifier);
    }

    public static FundsConfirmationConsentEntity createValidConsentEntity(String apiClientId, FRAccountIdentifier accountIdentifier) {
        final FundsConfirmationConsentEntity fundsConfirmationConsentEntity = new FundsConfirmationConsentEntity();
        fundsConfirmationConsentEntity.setApiClientId(apiClientId);
        fundsConfirmationConsentEntity.setStatus(OBExternalRequestStatus1Code.AWAITINGAUTHORISATION.toString());
        fundsConfirmationConsentEntity.setRequestVersion(OBVersion.v3_1_10);
        final OBFundsConfirmationConsent1 fundsConfirmationConsent1 = new OBFundsConfirmationConsent1();
        fundsConfirmationConsent1.setData(
                new OBFundsConfirmationConsentData1()
                        .expirationDateTime(DateTime.now().plusDays(30))
                        .debtorAccount(
                                new OBCashAccount3()
                                        .schemeName(accountIdentifier.getSchemeName())
                                        .identification(accountIdentifier.getIdentification())
                                        .name(accountIdentifier.getName())
                                        .secondaryIdentification(accountIdentifier.getSecondaryIdentification())
                        )
        );
        fundsConfirmationConsentEntity.setRequestObj(FRFundsConfirmationConsentConverter.toFRFundsConfirmationConsent(fundsConfirmationConsent1));
        return fundsConfirmationConsentEntity;
    }
}
