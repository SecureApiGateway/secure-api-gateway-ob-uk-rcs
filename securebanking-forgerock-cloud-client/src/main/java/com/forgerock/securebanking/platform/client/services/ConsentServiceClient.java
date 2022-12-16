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
package com.forgerock.securebanking.platform.client.services;

import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.ConsentClientDecisionRequest;
import com.forgerock.securebanking.platform.client.models.ConsentClientDetailsRequest;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsentServiceClient implements ConsentServiceInterface {

    private ConsentService consentService;

    public ConsentServiceClient(ConsentService consentService) {
        this.consentService = consentService;
    }

    @Override
    public JsonObject getConsent(ConsentClientDetailsRequest consentClientRequest) throws ExceptionClient {
        String intentId = consentClientRequest.getIntentId();
        log.debug("Retrieving the intent Id '{}", intentId);

        if (IntentType.identify(intentId) != null) {
            log.debug("Intent type: '{}' with ID '{}'", IntentType.identify(intentId), intentId);
            return consentService.getConsent(consentClientRequest);
        } else {
            String message = String.format("Invalid type for intent ID: '%s'", intentId);
            log.error(message);
            throw new ExceptionClient(consentClientRequest, ErrorType.UNKNOWN_INTENT_TYPE, message);
        }
    }

    @Override
    public JsonObject updateConsent(ConsentClientDecisionRequest consentClientDecisionRequest) throws ExceptionClient {
        String intentId = consentClientDecisionRequest.getIntentId();
        log.debug("Updating the intent Id '{}", consentClientDecisionRequest.getIntentId());

        if (IntentType.identify(intentId) != null) {
            log.debug("Intent type: '{}' with ID '{}'", IntentType.identify(intentId), intentId);
            return consentService.updateConsent(consentClientDecisionRequest);
        } else {
            String message = String.format("Invalid type to update for intent ID: '%s'", intentId);
            log.error(message);
            throw new ExceptionClient(consentClientDecisionRequest, ErrorType.UNKNOWN_INTENT_TYPE, message);
        }
    }
}
