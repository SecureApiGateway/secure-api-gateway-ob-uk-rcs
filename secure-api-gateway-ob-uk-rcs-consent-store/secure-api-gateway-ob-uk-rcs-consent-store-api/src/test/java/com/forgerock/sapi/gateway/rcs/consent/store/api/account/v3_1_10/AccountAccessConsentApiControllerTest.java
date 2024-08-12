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
package com.forgerock.sapi.gateway.rcs.consent.store.api.account.v3_1_10;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRReadConsent;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.api.BaseControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AuthoriseAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.account.OBReadConsent1;
import uk.org.openbanking.datamodel.v3.account.OBReadConsent1Data;
import uk.org.openbanking.datamodel.v3.account.OBRisk2;
import uk.org.openbanking.datamodel.v3.common.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;

public class AccountAccessConsentApiControllerTest extends BaseControllerTest<AccountAccessConsent, CreateAccountAccessConsentRequest, AuthoriseAccountAccessConsentRequest> {

    public static final List<String> TEST_AUTHORISED_ACCS = List.of("acc-1", "acc-2");

    @Autowired
    @Qualifier("internalAccountAccessConsentService")
    private AccountAccessConsentService accountAccessConsentService;

    protected AccountAccessConsentApiControllerTest() {
        super(AccountAccessConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "account-access-consents";
    }

    @Override
    protected String createConsentEntityForVersionValidation(String apiClient, OBVersion version) {
        final AccountAccessConsentEntity consent = new AccountAccessConsentEntity();
        consent.setApiClientId(apiClient);
        consent.setRequestVersion(version);
        consent.setRequestObj(createFRConsent());
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        return accountAccessConsentService.createConsent(consent).getId();
    }

    @Override
    protected CreateAccountAccessConsentRequest buildCreateConsentRequest(String apiClientId) {
        final CreateAccountAccessConsentRequest createRequest = new CreateAccountAccessConsentRequest();
        createRequest.setApiClientId(apiClientId);
        createRequest.setConsentRequest(createFRConsent());
        return createRequest;
    }

    private static FRReadConsent createFRConsent() {
        return FRReadConsentConverter.toFRReadConsent(new OBReadConsent1()
                .data(new OBReadConsent1Data().permissions(List.of(OBExternalPermissions1Code.READACCOUNTSBASIC)))
                .risk(new OBRisk2()));
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

    @Test
    void deleteConsent() {
        final AccountAccessConsent consent = createConsent(TEST_API_CLIENT_1);
        final ResponseEntity<Void> deleteConsentResponse = deleteConsent(consent.getId(), consent.getApiClientId(), Void.class);
        assertThat(deleteConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateConsentNotFoundErrorResponse(consent.getId(), makeGetRequest(consent.getId(), consent.getApiClientId(), OBErrorResponse1.class));
    }

    @Test
    void failToDeleteConsentBelongingToDifferentApiClient() {
        final AccountAccessConsent consent = createConsent(TEST_API_CLIENT_1);
        final ResponseEntity<OBErrorResponse1> deleteConsentResponse = deleteConsent(consent.getId(), "different-client", OBErrorResponse1.class);
        validateInvalidPermissionsErrorResponse(consent.getId(), deleteConsentResponse);
    }

    @Test
    void failToDeleteConsentThatHasBeenDeleted() {
        final AccountAccessConsent consent = createConsent(TEST_API_CLIENT_1);
        deleteConsent(consent.getId(), consent.getApiClientId(), Void.class);
        validateConsentNotFoundErrorResponse(consent.getId(), deleteConsent(consent.getId(), consent.getApiClientId(), OBErrorResponse1.class));
    }
}
