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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domesticscheduled;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticScheduledConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticScheduledConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.CreateDomesticScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticScheduledPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DomesticScheduledPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticScheduledConsentTestDataFactory;

import java.util.UUID;


public abstract class BaseDomesticScheduledPaymentConsentApiControllerTest extends BasePaymentConsentApiControllerTest<DomesticScheduledPaymentConsent, CreateDomesticScheduledPaymentConsentRequest> {

    @Autowired
    @Qualifier("internalDomesticScheduledPaymentConsentService")
    private DomesticScheduledPaymentConsentService consentService;

    public BaseDomesticScheduledPaymentConsentApiControllerTest() {
        super(DomesticScheduledPaymentConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "domestic-scheduled-payment-consents";
    }

    @Override
    protected String createConsentEntityForVersionValidation(String apiClient, OBVersion version) {
        final DomesticScheduledPaymentConsentEntity consent = new DomesticScheduledPaymentConsentEntity();
        consent.setApiClientId(apiClient);
        consent.setRequestVersion(version);
        consent.setRequestObj(createFRConsent());
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusMinutes(5));
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        return consentService.createConsent(consent).getId();
    }

    @Override
    protected CreateDomesticScheduledPaymentConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateDomesticScheduledPaymentConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateDomesticScheduledPaymentConsentRequest buildCreateDomesticScheduledPaymentConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateDomesticScheduledPaymentConsentRequest createDomesticScheduledPaymentConsentRequest = new CreateDomesticScheduledPaymentConsentRequest();
        final FRWriteDomesticScheduledConsent frWriteDomesticScheduledConsent = createFRConsent();
        createDomesticScheduledPaymentConsentRequest.setConsentRequest(frWriteDomesticScheduledConsent);
        createDomesticScheduledPaymentConsentRequest.setApiClientId(apiClientId);
        createDomesticScheduledPaymentConsentRequest.setIdempotencyKey(idempotencyKey);
        return createDomesticScheduledPaymentConsentRequest;
    }

    private static FRWriteDomesticScheduledConsent createFRConsent() {
        final OBWriteDomesticScheduledConsent4 paymentConsent = OBWriteDomesticScheduledConsentTestDataFactory.aValidOBWriteDomesticScheduledConsent4();
        final FRWriteDomesticScheduledConsent frWriteDomesticScheduledConsent = FRWriteDomesticScheduledConsentConverter.toFRWriteDomesticScheduledConsent(paymentConsent);
        return frWriteDomesticScheduledConsent;
    }
}