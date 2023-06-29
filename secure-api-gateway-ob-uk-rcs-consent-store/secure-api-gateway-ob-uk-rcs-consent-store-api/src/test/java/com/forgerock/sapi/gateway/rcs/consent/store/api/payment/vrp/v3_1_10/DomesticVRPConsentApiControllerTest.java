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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.vrp.v3_1_10;

import java.util.UUID;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVRPConsentConverters;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.vrp.v3_1_10.CreateDomesticVRPConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentApiControllerTest;

import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpConsentRequestTestDataFactory;


public class DomesticVRPConsentApiControllerTest extends BasePaymentConsentApiControllerTest<DomesticVRPConsent, CreateDomesticVRPConsentRequest> {

    public DomesticVRPConsentApiControllerTest() {
        super(DomesticVRPConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "domestic-vrp-consents";
    }

    @Override
    protected CreateDomesticVRPConsentRequest buildCreateConsentRequest(String apiClientId) {
        return buildCreateDomesticVRPConsentRequest(apiClientId, UUID.randomUUID().toString());
    }

    private static CreateDomesticVRPConsentRequest buildCreateDomesticVRPConsentRequest(String apiClientId, String idempotencyKey) {
        final CreateDomesticVRPConsentRequest createDomesticVRPConsentRequest = new CreateDomesticVRPConsentRequest();
        final OBDomesticVRPConsentRequest paymentConsent = OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest();
        createDomesticVRPConsentRequest.setConsentRequest(FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(paymentConsent));
        createDomesticVRPConsentRequest.setApiClientId(apiClientId);
        createDomesticVRPConsentRequest.setIdempotencyKey(idempotencyKey);
        return createDomesticVRPConsentRequest;
    }

}