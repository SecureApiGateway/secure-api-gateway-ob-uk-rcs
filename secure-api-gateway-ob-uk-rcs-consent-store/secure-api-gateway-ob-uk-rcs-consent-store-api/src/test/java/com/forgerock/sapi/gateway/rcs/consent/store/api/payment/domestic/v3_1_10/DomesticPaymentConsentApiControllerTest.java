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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domestic.v3_1_10;

import java.util.UUID;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConsentConverter;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory;


public class DomesticPaymentConsentApiControllerTest extends BasePaymentConsentApiControllerTest<DomesticPaymentConsent, CreateDomesticPaymentConsentRequest> {

    public DomesticPaymentConsentApiControllerTest() {
        super(DomesticPaymentConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "domestic-payment-consents";
    }

    @Override
    protected CreateDomesticPaymentConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateDomesticPaymentConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateDomesticPaymentConsentRequest buildCreateDomesticPaymentConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateDomesticPaymentConsentRequest createDomesticPaymentConsentRequest = new CreateDomesticPaymentConsentRequest();
        final OBWriteDomesticConsent4 paymentConsent = OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4();
        createDomesticPaymentConsentRequest.setConsentRequest(FRWriteDomesticConsentConverter.toFRWriteDomesticConsent(paymentConsent));
        createDomesticPaymentConsentRequest.setApiClientId(apiClientId);
        createDomesticPaymentConsentRequest.setIdempotencyKey(idempotencyKey);
        return createDomesticPaymentConsentRequest;
    }

}