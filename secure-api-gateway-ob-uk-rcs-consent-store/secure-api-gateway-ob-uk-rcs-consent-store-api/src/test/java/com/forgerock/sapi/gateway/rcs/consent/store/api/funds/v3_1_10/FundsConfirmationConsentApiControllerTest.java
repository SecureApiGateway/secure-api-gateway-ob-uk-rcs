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
package com.forgerock.sapi.gateway.rcs.consent.store.api.funds.v3_1_10;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.joda.time.DateTime;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.funds.FRFundsConfirmationConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.api.BaseControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.AuthoriseFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.CreateFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;

import uk.org.openbanking.datamodel.common.OBCashAccount3;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsent1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsentData1;

/**
 * Test for {@link FundsConfirmationConsentApiController}
 */
public class FundsConfirmationConsentApiControllerTest extends BaseControllerTest<FundsConfirmationConsent, CreateFundsConfirmationConsentRequest, AuthoriseFundsConfirmationConsentRequest> {


    protected FundsConfirmationConsentApiControllerTest() {
        super(FundsConfirmationConsent.class);
    }

    @PostConstruct
    public void postConstruct() {
        apiBaseUrl = "http://localhost:" + port + "/consent/store/v3.1.10/" + getControllerEndpointName();
    }

    @Override
    protected String getControllerEndpointName() {
        return "funds-confirmation-consents";
    }

    @Override
    protected CreateFundsConfirmationConsentRequest buildCreateConsentRequest(String apiClientId) {
        final CreateFundsConfirmationConsentRequest createRequest = new CreateFundsConfirmationConsentRequest();
        createRequest.setApiClientId(apiClientId);
        final OBFundsConfirmationConsent1 fundsConfirmationConsent1 = new OBFundsConfirmationConsent1();
        fundsConfirmationConsent1.setData(
                new OBFundsConfirmationConsentData1()
                        .expirationDateTime(DateTime.now().plusDays(30))
                        .debtorAccount(
                                new OBCashAccount3()
                                        .schemeName("UK.OBIE.SortCodeAccountNumber")
                                        .identification("40400422390112")
                                        .name("Mrs B Smith")
                        )
        );
        createRequest.setConsentRequest(FRFundsConfirmationConsentConverter.toFRFundsConfirmationConsent(fundsConfirmationConsent1));
        return createRequest;
    }

    @Override
    protected void validateCreateConsentAgainstCreateRequest(FundsConfirmationConsent consent, CreateFundsConfirmationConsentRequest createConsentRequest) {
        FundsConfirmationConsentValidationHelpers.validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);
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
