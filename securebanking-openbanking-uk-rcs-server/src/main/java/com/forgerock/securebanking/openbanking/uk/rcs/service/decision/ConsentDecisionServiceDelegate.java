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

import com.forgerock.securebanking.openbanking.uk.common.api.meta.IntentType;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_REQUEST_INVALID;

@Slf4j
@Service
public class ConsentDecisionServiceDelegate {

    private final AccountAccessConsentDecisionService accountAccessConsentDecisionService;
    private final DomesticPaymentConsentDecisionService domesticPaymentConsentDecisionService;

    public ConsentDecisionServiceDelegate(
            AccountAccessConsentDecisionService accountAccessConsentDecisionService,
            DomesticPaymentConsentDecisionService domesticPaymentConsentDecisionService) {
        this.accountAccessConsentDecisionService = accountAccessConsentDecisionService;
        this.domesticPaymentConsentDecisionService = domesticPaymentConsentDecisionService;;
    }

    public ConsentDecisionService getConsentDecisionService(String intentId) throws OBErrorException {
        log.debug("Intent ID: '{}'", intentId);
        IntentType intentType = IntentType.identify(intentId);
        if (intentType == null) {
            log.error("Invalid intent ID '{}'", intentId);
            throw new OBErrorException(RCS_CONSENT_REQUEST_INVALID, "Invalid intent ID: '" + intentId + "'");
        }
        switch (intentType) {
            case ACCOUNT_ACCESS_CONSENT -> {
                return accountAccessConsentDecisionService;
            }
            case PAYMENT_DOMESTIC_CONSENT -> {
                return domesticPaymentConsentDecisionService;
            }
            default -> {
                log.error("Unsupported intent ID '{}'", intentId);
                throw new OBErrorException(RCS_CONSENT_REQUEST_INVALID, "Unsupported intent ID: '" + intentId + "'");
            }
        }
    }
}