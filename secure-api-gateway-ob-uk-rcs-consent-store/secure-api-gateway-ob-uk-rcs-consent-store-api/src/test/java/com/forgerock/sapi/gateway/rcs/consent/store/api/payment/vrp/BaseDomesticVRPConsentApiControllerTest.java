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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.vrp;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVRPConsentConverters;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVRPConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.BaseControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.PaymentConsentValidationHelpers;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.CreateDomesticVRPConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.vrp.DomesticVRPConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.vrp.DomesticVRPConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpConsentRequestTestDataFactory;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public abstract class BaseDomesticVRPConsentApiControllerTest extends BaseControllerTest<DomesticVRPConsent, CreateDomesticVRPConsentRequest, AuthorisePaymentConsentRequest> {

    private static final String TEST_DEBTOR_ACC_ID = "acc-435345";

    @Autowired
    @Qualifier("internalDomesticVRPConsentService")
    private DomesticVRPConsentService consentService;

    public BaseDomesticVRPConsentApiControllerTest() {
        super(DomesticVRPConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "domestic-vrp-consents";
    }

    @Override
    protected String createConsentEntityForVersionValidation(String apiClient, OBVersion version) {
        final DomesticVRPConsentEntity consent = new DomesticVRPConsentEntity();
        consent.setApiClientId(apiClient);
        consent.setRequestVersion(version);
        consent.setRequestObj(createFRConsent());
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusMinutes(5));
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        return consentService.createConsent(consent).getId();
    }

    @Override
    protected CreateDomesticVRPConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateDomesticVRPConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    @Override
    protected AuthorisePaymentConsentRequest buildAuthoriseConsentRequest(DomesticVRPConsent consent, String resourceOwnerId) {
        final AuthorisePaymentConsentRequest authoriseReq = new AuthorisePaymentConsentRequest();
        authoriseReq.setConsentId(consent.getId());
        authoriseReq.setApiClientId(consent.getApiClientId());
        authoriseReq.setResourceOwnerId(resourceOwnerId);
        authoriseReq.setAuthorisedDebtorAccountId(TEST_DEBTOR_ACC_ID);
        return authoriseReq;
    }

    @Override
    protected void validateCreateConsentAgainstCreateRequest(DomesticVRPConsent consent, CreateDomesticVRPConsentRequest createConsentRequest) {
        PaymentConsentValidationHelpers.validateCreateConsentAgainstCreateRequest(consent, createConsentRequest, getControllerVersion());
    }

    @Override
    protected void validateAuthorisedConsent(DomesticVRPConsent authorisedConsent, AuthorisePaymentConsentRequest authoriseConsentReq, DomesticVRPConsent originalConsent) {
        PaymentConsentValidationHelpers.validateAuthorisedConsent(authorisedConsent, authoriseConsentReq, originalConsent);
    }

    @Override
    protected void validateRejectedConsent(DomesticVRPConsent rejectedConsent, RejectConsentRequest rejectConsentRequest, DomesticVRPConsent originalConsent) {
        PaymentConsentValidationHelpers.validateRejectedConsent(rejectedConsent, rejectConsentRequest, originalConsent);
    }

    private static CreateDomesticVRPConsentRequest buildCreateDomesticVRPConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateDomesticVRPConsentRequest createDomesticVRPConsentRequest = new CreateDomesticVRPConsentRequest();
        final FRDomesticVRPConsent frDomesticVRPConsent = createFRConsent();
        createDomesticVRPConsentRequest.setConsentRequest(frDomesticVRPConsent);
        createDomesticVRPConsentRequest.setApiClientId(apiClientId);
        createDomesticVRPConsentRequest.setIdempotencyKey(idempotencyKey);
        return createDomesticVRPConsentRequest;
    }

    private static FRDomesticVRPConsent createFRConsent() {
        final OBDomesticVRPConsentRequest paymentConsent = OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest();
        final FRDomesticVRPConsent frDomesticVRPConsent = FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(paymentConsent);
        return frDomesticVRPConsent;
    }

    @Test
    void deleteConsent() {
        final DomesticVRPConsent consent = createConsent(TEST_API_CLIENT_1);
        final ResponseEntity<Void> deleteConsentResponse = deleteConsent(consent.getId(), consent.getApiClientId(), Void.class);
        assertThat(deleteConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateConsentNotFoundErrorResponse(consent.getId(), makeGetRequest(consent.getId(), consent.getApiClientId(), OBErrorResponse1.class));
    }

    @Test
    void failToDeleteConsentBelongingToDifferentApiClient() {
        final DomesticVRPConsent consent = createConsent(TEST_API_CLIENT_1);
        final ResponseEntity<OBErrorResponse1> deleteConsentResponse = deleteConsent(consent.getId(), "different-client", OBErrorResponse1.class);
        validateInvalidPermissionsErrorResponse(consent.getId(), deleteConsentResponse);
    }

    @Test
    void failToDeleteConsentThatHasBeenDeleted() {
        final DomesticVRPConsent consent = createConsent(TEST_API_CLIENT_1);
        deleteConsent(consent.getId(), consent.getApiClientId(), Void.class);
        validateConsentNotFoundErrorResponse(consent.getId(), deleteConsent(consent.getId(), consent.getApiClientId(), OBErrorResponse1.class));
    }

}