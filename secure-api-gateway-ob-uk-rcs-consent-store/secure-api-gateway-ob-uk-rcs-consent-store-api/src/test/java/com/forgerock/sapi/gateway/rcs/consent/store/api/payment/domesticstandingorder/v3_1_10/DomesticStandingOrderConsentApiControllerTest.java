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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domesticstandingorder.v3_1_10;

import java.util.UUID;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticStandingOrderConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.CreateDomesticStandingOrderConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticStandingOrderConsentTestDataFactory;

public class DomesticStandingOrderConsentApiControllerTest extends BasePaymentConsentApiControllerTest<DomesticStandingOrderConsent, CreateDomesticStandingOrderConsentRequest> {

    public DomesticStandingOrderConsentApiControllerTest() {
        super(DomesticStandingOrderConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "domestic-standing-order-consents";
    }

    @Override
    protected CreateDomesticStandingOrderConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateDomesticStandingOrderConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateDomesticStandingOrderConsentRequest buildCreateDomesticStandingOrderConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateDomesticStandingOrderConsentRequest createDomesticStandingOrderConsentRequest = new CreateDomesticStandingOrderConsentRequest();
        final OBWriteDomesticStandingOrderConsent5 paymentConsent = OBWriteDomesticStandingOrderConsentTestDataFactory.aValidOBWriteDomesticStandingOrderConsent5();
        createDomesticStandingOrderConsentRequest.setConsentRequest(FRWriteDomesticStandingOrderConsentConverter.toFRWriteDomesticStandingOrderConsent(paymentConsent));
        createDomesticStandingOrderConsentRequest.setApiClientId(apiClientId);
        createDomesticStandingOrderConsentRequest.setIdempotencyKey(idempotencyKey);
        return createDomesticStandingOrderConsentRequest;
    }
}
