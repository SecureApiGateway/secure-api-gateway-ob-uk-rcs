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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.payment.domestic;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.payment.BasePaymentConsentDecisionService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.payment.BasePaymentConsentDecisionServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticStandingOrderConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DomesticStandingOrderConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentConsentService;

@ExtendWith(MockitoExtension.class)
public class DomesticStandingOrderConsentDecisionServiceTest extends BasePaymentConsentDecisionServiceTest<DomesticStandingOrderConsentEntity> {

    @Mock
    private DomesticStandingOrderConsentService domesticStandingOrderService;

    @InjectMocks
    private DomesticStandingOrderConsentDecisionService domesticStandingOrderDecisionService;


    @Override
    protected PaymentConsentService<DomesticStandingOrderConsentEntity, PaymentAuthoriseConsentArgs> getPaymentConsentService() {
        return domesticStandingOrderService;
    }

    @Override
    protected BasePaymentConsentDecisionService<DomesticStandingOrderConsentEntity> getConsentDecisionService() {
        return domesticStandingOrderDecisionService;
    }
}