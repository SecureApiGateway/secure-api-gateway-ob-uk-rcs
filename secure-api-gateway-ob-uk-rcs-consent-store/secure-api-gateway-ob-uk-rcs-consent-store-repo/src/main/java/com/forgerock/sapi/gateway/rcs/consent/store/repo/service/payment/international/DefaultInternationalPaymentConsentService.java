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

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.InternationalPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.payment.PaymentConsentRepository;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.BasePaymentConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

public class DefaultInternationalPaymentConsentService extends BasePaymentConsentService<InternationalPaymentConsentEntity, PaymentAuthoriseConsentArgs> implements InternationalPaymentConsentService {

    public DefaultInternationalPaymentConsentService(PaymentConsentRepository<InternationalPaymentConsentEntity> repo) {
        super(repo, IntentType.PAYMENT_INTERNATIONAL_CONSENT::generateIntentId);
    }
}
