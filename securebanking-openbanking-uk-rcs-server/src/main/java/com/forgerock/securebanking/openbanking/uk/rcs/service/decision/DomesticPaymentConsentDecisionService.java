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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRDomesticPaymentConsent;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.DomesticPaymentConsentDecision;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.DomesticPaymentConsentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.PAYMENT_CONSENT_NOT_FOUND;
import static com.forgerock.securebanking.openbanking.uk.rcs.util.ConsentDecisionDeserializer.deserializeConsentDecision;

@Service
@Slf4j
public class DomesticPaymentConsentDecisionService implements ConsentDecisionService {

    private final DomesticPaymentConsentService paymentConsentService;
    private final ObjectMapper objectMapper;
    private final PaymentConsentDecisionUpdater paymentConsentDecisionUpdater;

    public DomesticPaymentConsentDecisionService(DomesticPaymentConsentService paymentConsentService,
                                                 ObjectMapper objectMapper,
                                                 PaymentConsentDecisionUpdater paymentConsentDecisionUpdater) {
        this.paymentConsentService = paymentConsentService;
        this.objectMapper = objectMapper;
        this.paymentConsentDecisionUpdater = paymentConsentDecisionUpdater;
    }

    @Override
    public void processConsentDecision(String intentId, String consentDecisionSerialised, boolean decision) throws OBErrorException {
        FRDomesticPaymentConsent paymentConsent = getDomesticPaymentConsent(intentId);
        DomesticPaymentConsentDecision consentDecision = deserializeConsentDecision(consentDecisionSerialised,
                objectMapper, DomesticPaymentConsentDecision.class);
        paymentConsentDecisionUpdater.applyUpdate(
                paymentConsent.getUserId(),
                consentDecision.getAccountId(),
                decision,
                paymentConsentService::updateConsent,
                paymentConsent);
    }

    @Override
    public String getTppIdBehindConsent(String intentId) throws OBErrorException {
        return getDomesticPaymentConsent(intentId).getPispId();
    }

    @Override
    public String getUserIdBehindConsent(String intentId) throws OBErrorException {
        return getDomesticPaymentConsent(intentId).getUserId();
    }

    private FRDomesticPaymentConsent getDomesticPaymentConsent(String intentId) throws OBErrorException {
        FRDomesticPaymentConsent paymentConsent = paymentConsentService.getConsent(intentId);
        if (paymentConsent == null) {
            log.error("The PISP is referencing a payment request {} that doesn't exist", intentId);
            throw new OBErrorException(PAYMENT_CONSENT_NOT_FOUND, intentId);
        }
        return paymentConsent;
    }
}
