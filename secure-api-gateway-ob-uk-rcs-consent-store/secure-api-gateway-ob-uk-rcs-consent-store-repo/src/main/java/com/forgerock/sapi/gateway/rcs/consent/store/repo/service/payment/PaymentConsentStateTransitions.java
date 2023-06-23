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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment;

import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;

public class PaymentConsentStateTransitions {
    private static final MultiValueMap<String, String> PAYMENT_CONSENT_STATE_TRANSITIONS;

    public static final String AWAITING_AUTHORISATION = StatusEnum.AWAITINGAUTHORISATION.toString();

    public static final String AUTHORISED = StatusEnum.AUTHORISED.toString();

    public static final String CONSUMED = StatusEnum.CONSUMED.toString();

    public static final String REJECTED = StatusEnum.REJECTED.toString();

    static {
        PAYMENT_CONSENT_STATE_TRANSITIONS = new LinkedMultiValueMap<>();
        PAYMENT_CONSENT_STATE_TRANSITIONS.addAll(AWAITING_AUTHORISATION, List.of(AUTHORISED, REJECTED));
        PAYMENT_CONSENT_STATE_TRANSITIONS.addAll(AUTHORISED, List.of(CONSUMED, REJECTED));
    }


    public static MultiValueMap<String, String> getPaymentConsentStateTransitions() {
        return PAYMENT_CONSENT_STATE_TRANSITIONS;
    }
}
