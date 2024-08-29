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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domestic.v3_1_10;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v3_1_10.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory;


public class DomesticPaymentConsentApiControllerTest extends BasePaymentConsentApiControllerTest<DomesticPaymentConsent, CreateDomesticPaymentConsentRequest> {

    @Autowired
    @Qualifier("internalDomesticPaymentConsentService")
    private DomesticPaymentConsentService consentService;

    public DomesticPaymentConsentApiControllerTest() {
        super(DomesticPaymentConsent.class);
    }

    @Override
    protected OBVersion getControllerVersion() {
        return OBVersion.v3_1_10;
    }

    @Override
    protected String getControllerEndpointName() {
        return "domestic-payment-consents";
    }

    @Override
    protected String createConsentEntityForVersionValidation(String apiClient, OBVersion version) {
        final DomesticPaymentConsentEntity consent = new DomesticPaymentConsentEntity();
        consent.setApiClientId(apiClient);
        consent.setRequestVersion(version);
        consent.setRequestObj(createFRConsent());
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusMinutes(5));
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        return consentService.createConsent(consent).getId();
    }

    @Override
    protected CreateDomesticPaymentConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateDomesticPaymentConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateDomesticPaymentConsentRequest buildCreateDomesticPaymentConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateDomesticPaymentConsentRequest createDomesticPaymentConsentRequest = new CreateDomesticPaymentConsentRequest();
        final FRWriteDomesticConsent frWriteDomesticConsent = createFRConsent();
        createDomesticPaymentConsentRequest.setConsentRequest(frWriteDomesticConsent);
        createDomesticPaymentConsentRequest.setApiClientId(apiClientId);
        createDomesticPaymentConsentRequest.setIdempotencyKey(idempotencyKey);
        return createDomesticPaymentConsentRequest;
    }

    private static FRWriteDomesticConsent createFRConsent() {
        final OBWriteDomesticConsent4 paymentConsent = OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4();
        final FRWriteDomesticConsent frWriteDomesticConsent = FRWriteDomesticConsentConverter.toFRWriteDomesticConsent(paymentConsent);
        return frWriteDomesticConsent;
    }

}