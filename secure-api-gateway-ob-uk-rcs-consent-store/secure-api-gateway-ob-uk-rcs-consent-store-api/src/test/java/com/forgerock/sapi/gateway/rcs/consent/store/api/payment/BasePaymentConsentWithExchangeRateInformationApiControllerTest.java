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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.BaseCreateInternationalPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.BasePaymentConsentWithExchangeRateInformation;

public abstract class BasePaymentConsentWithExchangeRateInformationApiControllerTest<T extends BasePaymentConsentWithExchangeRateInformation, C extends BaseCreateInternationalPaymentConsentRequest> extends BasePaymentConsentApiControllerTest<T, C> {

    public BasePaymentConsentWithExchangeRateInformationApiControllerTest(Class<T> consentClass) {
        super(consentClass);
    }

    @Override
    protected void validateCreateConsentAgainstCreateRequest(T consent, C createConsentRequest) {
        PaymentConsentWithExchangeRateInformationValidationHelpers.validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);
    }

}
