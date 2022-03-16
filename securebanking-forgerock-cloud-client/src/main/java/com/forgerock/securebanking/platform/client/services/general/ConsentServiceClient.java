/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.securebanking.platform.client.services.general;

import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentDecision;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentRequest;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentRequest;
import com.forgerock.securebanking.platform.client.models.general.Consent;
import com.forgerock.securebanking.platform.client.models.general.ConsentDecision;
import com.forgerock.securebanking.platform.client.models.general.ConsentRequest;
import com.forgerock.securebanking.platform.client.services.accounts.AccountConsentService;
import com.forgerock.securebanking.platform.client.services.domestic.payments.DomesticPaymentConsentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsentServiceClient implements ConsentServiceInterface {

    private AccountConsentService accountConsentService;
    private DomesticPaymentConsentService domesticPaymentConsentService;

    public ConsentServiceClient(AccountConsentService accountConsentService, DomesticPaymentConsentService domesticPaymentConsentService) {
        this.accountConsentService = accountConsentService;
        this.domesticPaymentConsentService = domesticPaymentConsentService;
    }

    @Override
    public Consent getConsent(ConsentRequest consentRequest) throws ExceptionClient {
        String intentId = consentRequest.getIntentId();
        log.debug("Retrieving the intent Id '{}", intentId);
        switch (IntentType.identify(intentId)) {
            case ACCOUNT_ACCESS_CONSENT:
                log.debug("Intent type: '{}' with ID '{}'", IntentType.ACCOUNT_ACCESS_CONSENT.name(), intentId);
                return accountConsentService.getConsent((AccountConsentRequest) consentRequest);
            case PAYMENT_DOMESTIC_CONSENT:
                log.debug("Intent type: '{}' with ID '{}'", IntentType.PAYMENT_DOMESTIC_CONSENT.name(), intentId);
                return domesticPaymentConsentService.getConsent((DomesticPaymentConsentRequest) consentRequest);
            default:
                String message = String.format("Invalid type for intent ID: '%s'", intentId);
                log.error(message);
                throw new ExceptionClient(consentRequest, ErrorType.UNKNOWN_INTENT_TYPE, message);
        }
    }

    @Override
    public Consent updateConsent(ConsentDecision consentDecision) throws ExceptionClient {
        String intentId = consentDecision.getIntentId();
        log.debug("Updating the intent Id '{}", consentDecision.getIntentId());
        switch (IntentType.identify(intentId)) {
            case ACCOUNT_ACCESS_CONSENT:
                log.debug("Intent type to update: '{}' with ID '{}'", IntentType.ACCOUNT_ACCESS_CONSENT.name(), intentId);
                return accountConsentService.updateConsent((AccountConsentDecision) consentDecision);
            case PAYMENT_DOMESTIC_CONSENT:
                log.debug("Intent type to update: '{}' with ID '{}'", IntentType.PAYMENT_DOMESTIC_CONSENT.name(), intentId);
                return domesticPaymentConsentService.updateConsent(consentDecision);
            default:
                String message = String.format("Invalid type to update for intent ID: '%s'", intentId);
                log.error(message);
                throw new ExceptionClient(consentDecision, ErrorType.UNKNOWN_INTENT_TYPE, message);
        }
    }
}
