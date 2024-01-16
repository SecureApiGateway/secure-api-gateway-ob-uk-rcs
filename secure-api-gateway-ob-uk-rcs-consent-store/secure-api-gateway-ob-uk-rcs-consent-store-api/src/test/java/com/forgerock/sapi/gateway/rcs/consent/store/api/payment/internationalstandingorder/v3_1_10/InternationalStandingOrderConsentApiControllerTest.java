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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalStandingOrderConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalstandingorder.v3_1_10.CreateInternationalStandingOrderConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalstandingorder.v3_1_10.InternationalStandingOrderConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;

import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent5;
import uk.org.openbanking.testsupport.payment.OBWriteInternationalStandingOrderConsentTestDataFactory;

public class InternationalStandingOrderConsentApiControllerTest extends BasePaymentConsentApiControllerTest<InternationalStandingOrderConsent, CreateInternationalStandingOrderConsentRequest> {

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

    private static CreateInternationalStandingOrderConsentRequest buildCreateInternationalStandingOrderConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateInternationalStandingOrderConsentRequest createInternationalStandingOrderConsentRequest = new CreateInternationalStandingOrderConsentRequest();
        final OBWriteInternationalStandingOrderConsent5 paymentConsent = OBWriteInternationalStandingOrderConsentTestDataFactory.aValidOBWriteInternationalStandingOrderConsent5();
        createInternationalStandingOrderConsentRequest.setConsentRequest(FRWriteInternationalStandingOrderConsentConverter.toFRWriteInternationalStandingOrderConsent(paymentConsent));
        createInternationalStandingOrderConsentRequest.setApiClientId(apiClientId);
        createInternationalStandingOrderConsentRequest.setIdempotencyKey(idempotencyKey);
        return createInternationalStandingOrderConsentRequest;
    }
}
