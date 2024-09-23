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
package com.forgerock.sapi.gateway.rcs.consent.store.api.funds;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.funds.FRFundsConfirmationConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.funds.FRFundsConfirmationConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.BaseControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.api.funds.v3_1_10.FundsConfirmationConsentApiController;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.AuthoriseFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.CreateFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.funds.FundsConfirmationConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.FundsConfirmationConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsent1;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsent1Data;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsent1DataDebtorAccount;

import java.util.UUID;

/**
 * Test for {@link FundsConfirmationConsentApiController}
 */
public abstract class BaseFundsConfirmationConsentApiControllerTest extends BaseControllerTest<FundsConfirmationConsent, CreateFundsConfirmationConsentRequest, AuthoriseFundsConfirmationConsentRequest> {

    @Autowired
    @Qualifier("internalFundsConfirmationConsentService")
    private FundsConfirmationConsentService fundsConfirmationConsentService;

    protected BaseFundsConfirmationConsentApiControllerTest() {
        super(FundsConfirmationConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "funds-confirmation-consents";
    }

    @Override
    protected String createConsentEntityForVersionValidation(String apiClient, OBVersion version) {
        final FundsConfirmationConsentEntity consent = new FundsConfirmationConsentEntity();
        consent.setApiClientId(apiClient);
        consent.setRequestVersion(version);
        consent.setRequestObj(createFRConsent());
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        return fundsConfirmationConsentService.createConsent(consent).getId();
    }

    @Override
    protected CreateFundsConfirmationConsentRequest buildCreateConsentRequest(String apiClientId) {
        final CreateFundsConfirmationConsentRequest createRequest = new CreateFundsConfirmationConsentRequest();
        createRequest.setApiClientId(apiClientId);
        final FRFundsConfirmationConsent frFundsConfirmationConsent = createFRConsent();
        createRequest.setConsentRequest(frFundsConfirmationConsent);
        return createRequest;
    }

    private static FRFundsConfirmationConsent createFRConsent() {
        final OBFundsConfirmationConsent1 fundsConfirmationConsent1 = new OBFundsConfirmationConsent1();
        fundsConfirmationConsent1.setData(
                new OBFundsConfirmationConsent1Data()
                        .expirationDateTime(DateTime.now().plusDays(30))
                        .debtorAccount(
                                new OBFundsConfirmationConsent1DataDebtorAccount()
                                        .schemeName("UK.OBIE.SortCodeAccountNumber")
                                        .identification("40400422390112")
                                        .name("Mrs B Smith")
                        )
        );
        return FRFundsConfirmationConsentConverter.toFRFundsConfirmationConsent(fundsConfirmationConsent1);
    }

    @Override
    protected void validateCreateConsentAgainstCreateRequest(FundsConfirmationConsent consent, CreateFundsConfirmationConsentRequest createConsentRequest) {
        FundsConfirmationConsentValidationHelpers.validateCreateConsentAgainstCreateRequest(consent, createConsentRequest, getControllerVersion());
    }

    @Override
    protected AuthoriseFundsConfirmationConsentRequest buildAuthoriseConsentRequest(FundsConfirmationConsent consent, String resourceOwnerId) {
        AuthoriseFundsConfirmationConsentRequest authoriseFundsConfirmationConsentRequest = new AuthoriseFundsConfirmationConsentRequest();
        authoriseFundsConfirmationConsentRequest.setConsentId(consent.getId());
        authoriseFundsConfirmationConsentRequest.setApiClientId(consent.getApiClientId());
        authoriseFundsConfirmationConsentRequest.setResourceOwnerId(resourceOwnerId);
        authoriseFundsConfirmationConsentRequest.setAuthorisedDebtorAccountId(UUID.randomUUID().toString());
        return authoriseFundsConfirmationConsentRequest;
    }

    @Override
    protected void validateAuthorisedConsent(FundsConfirmationConsent authorisedConsent, AuthoriseFundsConfirmationConsentRequest authoriseConsentReq, FundsConfirmationConsent originalConsent) {
        FundsConfirmationConsentValidationHelpers.validateAuthorisedConsent(authorisedConsent, authoriseConsentReq, originalConsent);
    }

    @Override
    protected void validateRejectedConsent(FundsConfirmationConsent rejectedConsent, RejectConsentRequest rejectConsentRequest, FundsConfirmationConsent originalConsent) {
        FundsConfirmationConsentValidationHelpers.validateRejectedConsent(rejectedConsent, rejectConsentRequest, originalConsent);
    }
}
