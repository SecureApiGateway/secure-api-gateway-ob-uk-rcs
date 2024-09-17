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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.v4;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentStateModel;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.org.openbanking.datamodel.v4.payment.OBPaymentConsentStatus;

import java.util.List;

/**
 * State model for Payment APIs: https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/pisp/domestic-payment-consents.html#payment-order-consent
 *
 * Payments are short-lived consents, and therefore do not support re-authentication.
 *
 * File payments have a different model, see {@link com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.file.FilePaymentConsentStateModel}
 */
public class PaymentConsentStateModel implements ConsentStateModel {

    public static final String AWAU = OBPaymentConsentStatus.AWAU.getValue();

    public static final String AUTH = OBPaymentConsentStatus.AUTH.getValue();

    public static final String COND = OBPaymentConsentStatus.COND.getValue();

    public static final String RJCT = OBPaymentConsentStatus.RJCT.getValue();

    private static final PaymentConsentStateModel INSTANCE = new PaymentConsentStateModel();

    public static PaymentConsentStateModel getInstance() {
        return INSTANCE;
    }

    private final MultiValueMap<String, String> stateTransitions;

    private PaymentConsentStateModel() {
        stateTransitions = new LinkedMultiValueMap<>();
        stateTransitions.addAll(AWAU, List.of(AUTH, RJCT));
        stateTransitions.addAll(AUTH, List.of(COND));
    }

    @Override
    public String getInitialConsentStatus() {
        return AWAU;
    }

    @Override
    public String getAuthorisedConsentStatus() {
        return AUTH;
    }

    @Override
    public String getRejectedConsentStatus() {
        return RJCT;
    }

    @Override
    public String getRevokedConsentStatus() {
        return RJCT;
    }

    @Override
    public MultiValueMap<String, String> getValidStateTransitions() {
        return new LinkedMultiValueMap<>(stateTransitions);
    }

}
