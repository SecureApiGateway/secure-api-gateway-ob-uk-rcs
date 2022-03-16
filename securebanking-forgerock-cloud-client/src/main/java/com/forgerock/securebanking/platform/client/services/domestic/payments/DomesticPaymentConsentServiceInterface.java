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
package com.forgerock.securebanking.platform.client.services.domestic.payments;

import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentRequest;
import com.forgerock.securebanking.platform.client.models.general.Consent;
import com.forgerock.securebanking.platform.client.models.general.ConsentDecision;
import com.forgerock.securebanking.platform.client.services.general.ConsentService;

/**
 * Service interface for retrieving the details of consent requests.
 */
public interface DomesticPaymentConsentServiceInterface extends ConsentService {
    /**
     * Retrieves the specific {@link Consent} for the type of the consent from the platform.
     *
     * @param domesticPaymentConsentRequest A {@link DomesticPaymentConsentRequest} containing the required information to provide the consent details.
     * @return The underlying {@link Consent}, depending on the type of the consent.
     * @throws ExceptionClient if an error occurs.
     */
    Consent getConsent(DomesticPaymentConsentRequest domesticPaymentConsentRequest) throws ExceptionClient;

    /**
     * Update the specific {@link ConsentDecision} for the type of the consent on the platform.
     *
     * @param consentDecision A {@link ConsentDecision} containing the required information to provide the consent details.
     * @return The underlying {@link Consent}, depending on the type of the consent.
     * @throws ExceptionClient if an error occurs.
     */
    Consent updateConsent(ConsentDecision consentDecision) throws ExceptionClient;
}
