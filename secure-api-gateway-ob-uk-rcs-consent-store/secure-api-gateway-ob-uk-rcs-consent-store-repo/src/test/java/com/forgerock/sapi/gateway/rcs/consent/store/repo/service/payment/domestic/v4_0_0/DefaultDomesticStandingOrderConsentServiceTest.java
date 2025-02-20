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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.v4_0_0;

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
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteDomesticStandingOrderConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticStandingOrderConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.BasePaymentConsentServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DefaultDomesticStandingOrderConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.testsupport.v4.payment.OBWriteDomesticStandingOrderConsentTestDataFactory;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DefaultDomesticStandingOrderConsentServiceTest extends BasePaymentConsentServiceTest<DomesticStandingOrderConsentEntity> {

    @Autowired
    private DefaultDomesticStandingOrderConsentService service;

    @Override
    protected BaseConsentService<DomesticStandingOrderConsentEntity, PaymentAuthoriseConsentArgs> getConsentServiceToTest() {
        return service;
    }

    @Override
    protected DomesticStandingOrderConsentEntity getValidConsentEntity() {
        final String apiClientId = "test-client-987";
        return createValidConsentEntity(apiClientId);
    }

    public static DomesticStandingOrderConsentEntity createValidConsentEntity(String apiClientId) {
        return createValidConsentEntity(OBWriteDomesticStandingOrderConsentTestDataFactory.aValidOBWriteDomesticStandingOrderConsent5(), apiClientId);
    }

    public static DomesticStandingOrderConsentEntity createValidConsentEntity(OBWriteDomesticStandingOrderConsent5 obConsent, String apiClientId) {
        final DomesticStandingOrderConsentEntity domesticStandingOrderConsent = new DomesticStandingOrderConsentEntity();
        domesticStandingOrderConsent.setRequestVersion(OBVersion.v3_1_10);
        domesticStandingOrderConsent.setApiClientId(apiClientId);
        domesticStandingOrderConsent.setRequestObj(FRWriteDomesticStandingOrderConsentConverter.toFRWriteDomesticStandingOrderConsent(obConsent));
        domesticStandingOrderConsent.setStatus(OBPaymentConsentStatus.AWAITINGAUTHORISATION.toString());
        domesticStandingOrderConsent.setIdempotencyKey(UUID.randomUUID().toString());
        domesticStandingOrderConsent.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        domesticStandingOrderConsent.setCharges(List.of(
                FRCharge.builder().type("fee1")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.12", "GBP"))
                        .build(),
                FRCharge.builder().type("fee2")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.43", "GBP"))
                        .build())
        );
        return domesticStandingOrderConsent;
    }
}
