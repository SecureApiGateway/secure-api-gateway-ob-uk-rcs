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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRChargeBearerType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.InternationalPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class DefaultInternationalPaymentConsentServiceTest extends BaseInternationalPaymentConsentServiceTest<InternationalPaymentConsentEntity> {

    @Autowired
    private DefaultInternationalPaymentConsentService consentService;

    @Override
    protected BaseConsentService<InternationalPaymentConsentEntity, PaymentAuthoriseConsentArgs> getConsentServiceToTest() {
        return consentService;
    }

    @Override
    protected InternationalPaymentConsentEntity getValidConsentEntity() {
        final String apiClientId = "test-client-987";
        return createValidConsentEntity(apiClientId);
    }

    public static InternationalPaymentConsentEntity createValidConsentEntity(String apiClientId) {
        return createValidConsentEntity(OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5(), apiClientId);
    }

    public static InternationalPaymentConsentEntity createValidConsentEntity(OBWriteInternationalConsent5 obConsent, String apiClientId) {
        final InternationalPaymentConsentEntity consent = new InternationalPaymentConsentEntity();
        consent.setRequestVersion(OBVersion.v3_1_10);
        consent.setApiClientId(apiClientId);
        consent.setRequestObj(FRWriteInternationalConsentConverter.toFRWriteInternationalConsent(obConsent));
        consent.setStatus(StatusEnum.AWAITINGAUTHORISATION.toString());
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        consent.setCharges(List.of(
                FRCharge.builder().type("fee1")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.15","GBP"))
                        .build(),
                FRCharge.builder().type("fee2")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.10","GBP"))
                        .build())
        );
        consent.setExchangeRateInformation(getExchangeRateInformation(obConsent.getData().getInitiation().getExchangeRateInformation()));
        return consent;
    }
}