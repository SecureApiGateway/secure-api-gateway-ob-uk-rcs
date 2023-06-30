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
package com.forgerock.sapi.gateway.rcs.consent.store.api.account.v3_1_10;

import java.util.List;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AuthoriseAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.api.BaseControllerTest;

import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadConsent1;
import uk.org.openbanking.datamodel.account.OBReadData1;
import uk.org.openbanking.datamodel.account.OBRisk2;

public class AccountAccessConsentApiControllerTest extends BaseControllerTest<AccountAccessConsent, CreateAccountAccessConsentRequest, AuthoriseAccountAccessConsentRequest> {

    public static final List<String> TEST_AUTHORISED_ACCS = List.of("acc-1", "acc-2");

    protected AccountAccessConsentApiControllerTest() {
        super(AccountAccessConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "account-access-consents";
    }

    @Override
    protected CreateAccountAccessConsentRequest buildCreateConsentRequest(String apiClientId) {
        final CreateAccountAccessConsentRequest createRequest = new CreateAccountAccessConsentRequest();
        createRequest.setApiClientId(apiClientId);
        createRequest.setConsentRequest(FRReadConsentConverter.toFRReadConsent(new OBReadConsent1()
                .data(new OBReadData1().permissions(List.of(OBExternalPermissions1Code.READACCOUNTSBASIC)))
                .risk(new OBRisk2())));
        return createRequest;
    }

    @Override
    protected void validateCreateConsentAgainstCreateRequest(AccountAccessConsent consent, CreateAccountAccessConsentRequest createConsentRequest) {
        AccountAccessConsentValidationHelpers.validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);
    }

    @Override
    protected AuthoriseAccountAccessConsentRequest buildAuthoriseConsentRequest(AccountAccessConsent consent, String resourceOwnerId) {
        final AuthoriseAccountAccessConsentRequest authoriseAccountAccessConsentRequest = new AuthoriseAccountAccessConsentRequest();
        authoriseAccountAccessConsentRequest.setConsentId(consent.getId());
        authoriseAccountAccessConsentRequest.setApiClientId(consent.getApiClientId());
        authoriseAccountAccessConsentRequest.setResourceOwnerId(resourceOwnerId);
        authoriseAccountAccessConsentRequest.setAuthorisedAccountIds(TEST_AUTHORISED_ACCS);
        return authoriseAccountAccessConsentRequest;
    }

    @Override
    protected void validateAuthorisedConsent(AccountAccessConsent authorisedConsent, AuthoriseAccountAccessConsentRequest authoriseConsentReq, AccountAccessConsent originalConsent) {
        AccountAccessConsentValidationHelpers.validateAuthorisedConsent(authorisedConsent, authoriseConsentReq, originalConsent);
    }

    @Override
    protected void validateRejectedConsent(AccountAccessConsent rejectedConsent, RejectConsentRequest rejectConsentRequest, AccountAccessConsent originalConsent) {
        AccountAccessConsentValidationHelpers.validateRejectedConsent(rejectedConsent, rejectConsentRequest, originalConsent);
    }
}
