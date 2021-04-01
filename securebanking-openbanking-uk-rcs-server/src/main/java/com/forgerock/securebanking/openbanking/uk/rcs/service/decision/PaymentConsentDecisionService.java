/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.service.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.PaymentConsentDecision;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRPaymentConsent;
import lombok.extern.slf4j.Slf4j;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.PAYMENT_CONSENT_NOT_FOUND;
import static com.forgerock.securebanking.openbanking.uk.rcs.util.ConsentDecisionDeserializer.deserializeConsentDecision;

/**
 * Abstract class providing a common way to retrieve and update consents for the various payment types.
 */
@Slf4j
public abstract class PaymentConsentDecisionService implements ConsentDecisionService {

    private final PaymentConsentService paymentConsentService;
    private final ObjectMapper objectMapper;
    private final PaymentConsentDecisionUpdater paymentConsentDecisionUpdater;

    public PaymentConsentDecisionService(PaymentConsentService paymentConsentService,
                                         ObjectMapper objectMapper,
                                         PaymentConsentDecisionUpdater paymentConsentDecisionUpdater) {
        this.paymentConsentService = paymentConsentService;
        this.objectMapper = objectMapper;
        this.paymentConsentDecisionUpdater = paymentConsentDecisionUpdater;
    }

    public void processConsentDecision(String intentId, String consentDecisionSerialised, boolean decision) throws OBErrorException {
        FRPaymentConsent paymentConsent = getPaymentConsent(intentId);
        PaymentConsentDecision consentDecision = deserializeConsentDecision(consentDecisionSerialised,
                objectMapper, PaymentConsentDecision.class);
        paymentConsentDecisionUpdater.applyUpdate(
                paymentConsent.getResourceOwnerUsername(),
                consentDecision.getAccountId(),
                decision,
                paymentConsentService::updateConsent,
                paymentConsent);
    }

    public String getTppIdBehindConsent(String intentId) throws OBErrorException {
        return getPaymentConsent(intentId).getOauth2ClientId();
    }

    public String getUserIdBehindConsent(String intentId) throws OBErrorException {
        return getPaymentConsent(intentId).getResourceOwnerUsername();
    }

    private FRPaymentConsent getPaymentConsent(String intentId) throws OBErrorException {
        FRPaymentConsent paymentConsent = paymentConsentService.getConsent(intentId, getConsentClass());
        if (paymentConsent == null) {
            log.error("The PISP is referencing a payment request {} that doesn't exist", intentId);
            throw new OBErrorException(PAYMENT_CONSENT_NOT_FOUND, intentId);
        }
        return paymentConsent;
    }

    protected abstract Class<? extends FRPaymentConsent> getConsentClass();
}
