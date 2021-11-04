/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.platform.client.services;

import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.Consent;
import com.forgerock.securebanking.platform.client.models.ConsentDecision;
import com.forgerock.securebanking.platform.client.models.ConsentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsentServiceClient implements ConsentService {

    private AccountConsentService accountConsentService;

    public ConsentServiceClient(AccountConsentService accountConsentService) {
        this.accountConsentService = accountConsentService;
    }

    @Override
    public Consent getConsent(ConsentRequest consentRequest) throws ExceptionClient {
        String intentId = consentRequest.getIntentId();
        log.debug("Retrieving the intent Id '{}", intentId);
        switch (IntentType.identify(intentId)) {
            case ACCOUNT_ACCESS_CONSENT:
                log.debug("Intent type: '{}' with ID '{}'", IntentType.ACCOUNT_ACCESS_CONSENT.name(), intentId);
                return accountConsentService.getConsent(consentRequest);
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
                return accountConsentService.updateConsent(consentDecision);
            default:
                String message = String.format("Invalid type to update for intent ID: '%s'", intentId);
                log.error(message);
                throw new ExceptionClient(consentDecision, ErrorType.UNKNOWN_INTENT_TYPE, message);
        }
    }
}