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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.InternationalPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.payment.PaymentConsentRepository;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentServiceFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.version.ApiVersionValidator;

@Service
public class InternationalPaymentConsentServiceFactory extends ConsentServiceFactory<InternationalPaymentConsentEntity, PaymentAuthoriseConsentArgs, DefaultInternationalPaymentConsentService> {

    @Autowired
    public InternationalPaymentConsentServiceFactory(PaymentConsentRepository<InternationalPaymentConsentEntity> repo, ApiVersionValidator apiVersionValidator) {
        super(repo, apiVersionValidator);
    }

    @Override
    protected DefaultInternationalPaymentConsentService createBaseConsentService() {
        return new DefaultInternationalPaymentConsentService((PaymentConsentRepository)repo);
    }
}
