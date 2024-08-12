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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.internationalstandingorder.v3_1_10;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalStandingOrderConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalStandingOrderConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalstandingorder.v3_1_10.CreateInternationalStandingOrderConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalstandingorder.v3_1_10.InternationalStandingOrderConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.InternationalStandingOrderConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international.InternationalStandingOrderConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalStandingOrderConsent6;
import uk.org.openbanking.testsupport.payment.OBWriteInternationalStandingOrderConsentTestDataFactory;

public class InternationalStandingOrderConsentApiControllerTest extends BasePaymentConsentApiControllerTest<InternationalStandingOrderConsent, CreateInternationalStandingOrderConsentRequest> {

    @Autowired
    @Qualifier("internalInternationalStandingOrderConsentService")
    private InternationalStandingOrderConsentService consentService;

    public InternationalStandingOrderConsentApiControllerTest() {
        super(InternationalStandingOrderConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "international-standing-order-consents";
    }

    @Override
    protected CreateInternationalStandingOrderConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateInternationalStandingOrderConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    @Override
    protected String createConsentEntityForVersionValidation(String apiClient, OBVersion version) {
        final InternationalStandingOrderConsentEntity consent = new InternationalStandingOrderConsentEntity();
        consent.setApiClientId(apiClient);
        consent.setRequestVersion(version);
        consent.setRequestObj(createFRConsent());
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusMinutes(5));
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        return consentService.createConsent(consent).getId();
    }

    private static CreateInternationalStandingOrderConsentRequest buildCreateInternationalStandingOrderConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateInternationalStandingOrderConsentRequest createInternationalStandingOrderConsentRequest = new CreateInternationalStandingOrderConsentRequest();
        final FRWriteInternationalStandingOrderConsent frWriteInternationalStandingOrderConsent = createFRConsent();
        createInternationalStandingOrderConsentRequest.setConsentRequest(frWriteInternationalStandingOrderConsent);
        createInternationalStandingOrderConsentRequest.setApiClientId(apiClientId);
        createInternationalStandingOrderConsentRequest.setIdempotencyKey(idempotencyKey);
        return createInternationalStandingOrderConsentRequest;
    }

    private static FRWriteInternationalStandingOrderConsent createFRConsent() {
        final OBWriteInternationalStandingOrderConsent6 paymentConsent = OBWriteInternationalStandingOrderConsentTestDataFactory.aValidOBWriteInternationalStandingOrderConsent6();
        final FRWriteInternationalStandingOrderConsent frWriteInternationalStandingOrderConsent = FRWriteInternationalStandingOrderConsentConverter.toFRWriteInternationalStandingOrderConsent(paymentConsent);
        return frWriteInternationalStandingOrderConsent;
    }
}
