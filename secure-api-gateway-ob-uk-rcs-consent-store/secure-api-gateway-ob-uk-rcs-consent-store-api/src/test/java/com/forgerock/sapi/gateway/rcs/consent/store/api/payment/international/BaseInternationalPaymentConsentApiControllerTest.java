/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.international;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRWriteInternationalConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.CreateInternationalPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.InternationalPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.InternationalPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international.InternationalPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.testsupport.v3.payment.OBWriteInternationalConsentTestDataFactory;

public abstract class BaseInternationalPaymentConsentApiControllerTest extends BasePaymentConsentApiControllerTest<InternationalPaymentConsent, CreateInternationalPaymentConsentRequest> {

    @Autowired
    @Qualifier("internalInternationalPaymentConsentService")
    private InternationalPaymentConsentService consentService;

    public BaseInternationalPaymentConsentApiControllerTest() {
        super(InternationalPaymentConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "international-payment-consents";
    }

    @Override
    protected String createConsentEntityForVersionValidation(String apiClient, OBVersion version) {
        final InternationalPaymentConsentEntity consent = new InternationalPaymentConsentEntity();
        consent.setApiClientId(apiClient);
        consent.setRequestVersion(version);
        consent.setRequestObj(createFRConsent());
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusMinutes(5));
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        return consentService.createConsent(consent).getId();
    }

    @Override
    protected CreateInternationalPaymentConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateInternationalPaymentConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateInternationalPaymentConsentRequest buildCreateInternationalPaymentConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateInternationalPaymentConsentRequest createInternationalPaymentConsentRequest = new CreateInternationalPaymentConsentRequest();
        final FRWriteInternationalConsent frWriteInternationalConsent = createFRConsent();
        createInternationalPaymentConsentRequest.setConsentRequest(frWriteInternationalConsent);
        createInternationalPaymentConsentRequest.setApiClientId(apiClientId);
        createInternationalPaymentConsentRequest.setIdempotencyKey(idempotencyKey);
        return createInternationalPaymentConsentRequest;
    }

    private static FRWriteInternationalConsent createFRConsent() {
        final OBWriteInternationalConsent5 paymentConsent = OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5();
        final FRWriteInternationalConsent frWriteInternationalConsent = FRWriteInternationalConsentConverter.toFRWriteInternationalConsent(paymentConsent);
        return frWriteInternationalConsent;
    }
}
