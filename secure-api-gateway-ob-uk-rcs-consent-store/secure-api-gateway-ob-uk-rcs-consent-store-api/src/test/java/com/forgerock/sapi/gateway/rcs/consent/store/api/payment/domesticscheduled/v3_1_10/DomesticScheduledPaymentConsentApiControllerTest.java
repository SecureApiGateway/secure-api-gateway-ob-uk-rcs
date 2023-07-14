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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domesticscheduled.v3_1_10;

import java.util.UUID;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticScheduledConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.CreateDomesticScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticScheduledConsentTestDataFactory;


public class DomesticScheduledPaymentConsentApiControllerTest extends BasePaymentConsentApiControllerTest<DomesticScheduledPaymentConsent, CreateDomesticScheduledPaymentConsentRequest> {

    public DomesticScheduledPaymentConsentApiControllerTest() {
        super(DomesticScheduledPaymentConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "domestic-scheduled-payment-consents";
    }

    @Override
    protected CreateDomesticScheduledPaymentConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateDomesticScheduledPaymentConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateDomesticScheduledPaymentConsentRequest buildCreateDomesticScheduledPaymentConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateDomesticScheduledPaymentConsentRequest createDomesticScheduledPaymentConsentRequest = new CreateDomesticScheduledPaymentConsentRequest();
        final OBWriteDomesticScheduledConsent4 paymentConsent = OBWriteDomesticScheduledConsentTestDataFactory.aValidOBWriteDomesticScheduledConsent4();
        createDomesticScheduledPaymentConsentRequest.setConsentRequest(FRWriteDomesticScheduledConsentConverter.toFRWriteDomesticScheduledConsent(paymentConsent));
        createDomesticScheduledPaymentConsentRequest.setApiClientId(apiClientId);
        createDomesticScheduledPaymentConsentRequest.setIdempotencyKey(idempotencyKey);
        return createDomesticScheduledPaymentConsentRequest;
    }
}