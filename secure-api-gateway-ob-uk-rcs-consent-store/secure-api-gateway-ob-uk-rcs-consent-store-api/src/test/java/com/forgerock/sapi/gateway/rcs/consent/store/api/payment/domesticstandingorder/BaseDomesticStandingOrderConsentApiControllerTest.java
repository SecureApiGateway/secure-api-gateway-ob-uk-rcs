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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domesticstandingorder;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticStandingOrderConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticStandingOrderConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.CreateDomesticStandingOrderConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticStandingOrderConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DomesticStandingOrderConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticStandingOrderConsentTestDataFactory;

public abstract class BaseDomesticStandingOrderConsentApiControllerTest extends BasePaymentConsentApiControllerTest<DomesticStandingOrderConsent, CreateDomesticStandingOrderConsentRequest> {

    @Autowired
    @Qualifier("internalDomesticStandingOrderConsentService")
    private DomesticStandingOrderConsentService consentService;

    public BaseDomesticStandingOrderConsentApiControllerTest() {
        super(DomesticStandingOrderConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "domestic-standing-order-consents";
    }

    @Override
    protected String createConsentEntityForVersionValidation(String apiClient, OBVersion version) {
        final DomesticStandingOrderConsentEntity consent = new DomesticStandingOrderConsentEntity();
        consent.setApiClientId(apiClient);
        consent.setRequestVersion(version);
        consent.setRequestObj(createFRConsent());
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusMinutes(5));
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        return consentService.createConsent(consent).getId();
    }

    @Override
    protected CreateDomesticStandingOrderConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateDomesticStandingOrderConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateDomesticStandingOrderConsentRequest buildCreateDomesticStandingOrderConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateDomesticStandingOrderConsentRequest createDomesticStandingOrderConsentRequest = new CreateDomesticStandingOrderConsentRequest();
        final FRWriteDomesticStandingOrderConsent frWriteDomesticStandingOrderConsent = createFRConsent();
        createDomesticStandingOrderConsentRequest.setConsentRequest(frWriteDomesticStandingOrderConsent);
        createDomesticStandingOrderConsentRequest.setApiClientId(apiClientId);
        createDomesticStandingOrderConsentRequest.setIdempotencyKey(idempotencyKey);
        return createDomesticStandingOrderConsentRequest;
    }

    private static FRWriteDomesticStandingOrderConsent createFRConsent() {
        final OBWriteDomesticStandingOrderConsent5 paymentConsent = OBWriteDomesticStandingOrderConsentTestDataFactory.aValidOBWriteDomesticStandingOrderConsent5();
        final FRWriteDomesticStandingOrderConsent frWriteDomesticStandingOrderConsent = FRWriteDomesticStandingOrderConsentConverter.toFRWriteDomesticStandingOrderConsent(paymentConsent);
        return frWriteDomesticStandingOrderConsent;
    }
}
