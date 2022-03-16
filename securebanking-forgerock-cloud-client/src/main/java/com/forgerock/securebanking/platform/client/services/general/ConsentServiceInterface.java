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

import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentDecision;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentRequest;
import com.forgerock.securebanking.platform.client.models.general.Consent;
import com.forgerock.securebanking.platform.client.models.general.ConsentDecision;
import com.forgerock.securebanking.platform.client.models.general.ConsentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service interface for retrieving the details of consent requests.
 */
public interface ConsentServiceInterface {
    Logger log = LoggerFactory.getLogger(ConsentServiceInterface.class);

    /**
     * Retrieves the specific {@link Consent} for the type of the consent from the platform.
     *
     * @param accountConsentRequest A {@link ConsentRequest} containing the required information to provide the consent details.
     * @return The underlying {@link Consent}, depending on the type of the consent.
     * @throws ExceptionClient if an error occurs.
     */
    Consent getConsent(ConsentRequest accountConsentRequest) throws ExceptionClient;

    /**
     * Update the specific {@link ConsentDecision} for the type of the consent on the platform.
     *
     * @param consentDecision A {@link ConsentDecision} containing the required information to provide the consent details.
     * @return The underlying {@link Consent}, depending on the type of the consent.
     * @throws ExceptionClient if an error occurs.
     */
    Consent updateConsent(ConsentDecision consentDecision) throws ExceptionClient;
}
