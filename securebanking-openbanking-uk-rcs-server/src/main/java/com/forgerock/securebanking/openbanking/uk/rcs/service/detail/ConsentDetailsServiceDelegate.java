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
package com.forgerock.securebanking.openbanking.uk.rcs.service.detail;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.IntentType;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_REQUEST_INVALID;

@Slf4j
@Service
public class ConsentDetailsServiceDelegate {

    private final AccountConsentDetailsService accountConsentDetailsService;
    private final DomesticPaymentConsentDetailsService domesticPaymentConsentDetailsService;

    public ConsentDetailsServiceDelegate(AccountConsentDetailsService accountConsentDetailsService,
                                         DomesticPaymentConsentDetailsService domesticPaymentConsentDetailsService) {
        this.accountConsentDetailsService = accountConsentDetailsService;
        this.domesticPaymentConsentDetailsService = domesticPaymentConsentDetailsService;
    }

    public ConsentDetails getConsentDetails(ConsentDetailsRequest request) throws OBErrorException {
        String intentId = request.getIntentId();
        log.debug("Intent ID: '{}'", intentId);
        switch (IntentType.identify(intentId)) {
            case ACCOUNT_ACCESS_CONSENT:
                return accountConsentDetailsService.getConsentDetails(request);
            case PAYMENT_DOMESTIC_CONSENT:
                return domesticPaymentConsentDetailsService.getConsentDetails(request);
            default:
                log.error("Invalid intent ID: '{}'", intentId);
                throw new OBErrorException(RCS_CONSENT_REQUEST_INVALID, "Invalid intent ID: '" + intentId + "'");
        }
    }
}