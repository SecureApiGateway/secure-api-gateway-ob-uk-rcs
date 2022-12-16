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

import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.ConsentClientDecisionRequest;
import com.forgerock.securebanking.platform.client.models.ConsentClientDetailsRequest;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service interface for retrieving the details of consent requests.
 */
public interface ConsentServiceInterface {
    Logger log = LoggerFactory.getLogger(ConsentServiceInterface.class);

    /**
     * Retrieves the specific consent for the type of the consent from the platform.
     *
     * @param ConsentRequest A {@link ConsentClientDetailsRequest} containing the required information to provide the consent details.
     * @return The underlying Consent, depending on the type of the consent.
     * @throws ExceptionClient if an error occurs.
     */
    JsonObject getConsent(ConsentClientDetailsRequest ConsentRequest) throws ExceptionClient;

    /**
     * Update the specific {@link ConsentClientDecisionRequest} for the type of the consent on the platform.
     *
     * @param consentDecision A {@link ConsentClientDecisionRequest} containing the required information to provide the consent details.
     * @return The underlying Consent, depending on the type of the consent.
     * @throws ExceptionClient if an error occurs.
     */
    JsonObject updateConsent(ConsentClientDecisionRequest consentDecision) throws ExceptionClient;
}
