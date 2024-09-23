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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.international;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRChargeBearerType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.BasePaymentConsentWithExchangeRateInformationApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.CreateInternationalPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.InternationalPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.InternationalPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international.InternationalPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory;

import java.util.List;
import java.util.UUID;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international.BasePaymentServiceWithExchangeRateInformationTest.getExchangeRateInformation;

public abstract class BaseInternationalPaymentConsentApiControllerTest extends BasePaymentConsentWithExchangeRateInformationApiControllerTest<InternationalPaymentConsent, CreateInternationalPaymentConsentRequest> {

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
        consent.setRequestObj(FRWriteInternationalConsentConverter.toFRWriteInternationalConsent(OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5()));
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusMinutes(5));
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        return consentService.createConsent(consent).getId();
    }

    @Override
    protected CreateInternationalPaymentConsentRequest buildCreateConsentRequest(String apiClientId) {
        final CreateInternationalPaymentConsentRequest createConsentRequest = new CreateInternationalPaymentConsentRequest();
        final OBWriteInternationalConsent5 paymentConsent = OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5();
        final FRWriteInternationalConsent frConsent = FRWriteInternationalConsentConverter.toFRWriteInternationalConsent(paymentConsent);
        createConsentRequest.setConsentRequest(frConsent);
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
