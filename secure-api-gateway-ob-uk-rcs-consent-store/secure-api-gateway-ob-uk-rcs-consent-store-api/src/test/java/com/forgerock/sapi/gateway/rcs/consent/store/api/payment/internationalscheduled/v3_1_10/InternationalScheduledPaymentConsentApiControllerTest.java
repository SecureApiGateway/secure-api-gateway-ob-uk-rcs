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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.internationalscheduled.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international.BasePaymentServiceWithExchangeRateInformationTest.getExchangeRateInformation;

import java.util.List;
import java.util.UUID;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRChargeBearerType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalScheduledConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.CreateInternationalScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.InternationalScheduledPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentWithExchangeRateInformationApiControllerTest;

import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalScheduledConsent5;
import uk.org.openbanking.testsupport.payment.OBWriteInternationalScheduledConsentTestDataFactory;

public class InternationalScheduledPaymentConsentApiControllerTest extends BasePaymentConsentWithExchangeRateInformationApiControllerTest<InternationalScheduledPaymentConsent, CreateInternationalScheduledPaymentConsentRequest> {

    public InternationalScheduledPaymentConsentApiControllerTest() {
        super(InternationalScheduledPaymentConsent.class);
    }

    @Override
    protected String getControllerEndpointName() {
        return "international-scheduled-payment-consents";
    }

    @Override
    protected CreateInternationalScheduledPaymentConsentRequest buildCreateConsentRequest(String apiClientId) {
        final CreateInternationalScheduledPaymentConsentRequest createConsentRequest = new CreateInternationalScheduledPaymentConsentRequest();
        final OBWriteInternationalScheduledConsent5 paymentConsent = OBWriteInternationalScheduledConsentTestDataFactory.aValidOBWriteInternationalScheduledConsent5();
        createConsentRequest.setConsentRequest(FRWriteInternationalScheduledConsentConverter.toFRWriteInternationalScheduledConsent(paymentConsent));
        createConsentRequest.setApiClientId(apiClientId);
        createConsentRequest.setIdempotencyKey(UUID.randomUUID().toString());
        createConsentRequest.setCharges(List.of(
                FRCharge.builder().type("fee1")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.54","GBP"))
                        .build(),
                FRCharge.builder().type("fee2")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.10","GBP"))
                        .build())
        );
        createConsentRequest.setExchangeRateInformation(getExchangeRateInformation(paymentConsent.getData().getInitiation().getExchangeRateInformation()));

        return createConsentRequest;
    }
}
