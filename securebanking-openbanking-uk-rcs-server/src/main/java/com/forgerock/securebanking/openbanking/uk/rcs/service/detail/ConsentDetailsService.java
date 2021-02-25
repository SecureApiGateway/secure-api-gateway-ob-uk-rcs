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

import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_REQUEST_INVALID_PAYMENT_REQUEST;

/**
 * Service interface for retrieving the details of consent requests.
 */
public interface ConsentDetailsService {
    Logger log = LoggerFactory.getLogger(ConsentDetailsService.class);

    /**
     * Retrieves the specific {@link ConsentDetails} for the type of the consent. This is used to display the details
     * of the account to the user.
     *
     * @param request A {@link ConsentDetailsRequest} containing the required information to provide the consent details.
     * @return The underlying {@link ConsentDetails}, depending on the type of the consent.
     * @throws OBErrorException if an error occurs.
     */
    ConsentDetails getConsentDetails(ConsentDetailsRequest request) throws OBErrorException;

    /**
     * TPP making the consent request to the user should be same one that created the new payment consent in RS.
     */
    default void verifyTppCreatedPaymentConsent(String clientIdRequestingConsent,
                                                String clientIdThatCreatedPayment,
                                                String consentId) throws OBErrorException {
        //Verify the pisp is the same than the one that created this payment
        log.debug("PISP that is requesting consent in RCS Details service => '{}'", clientIdRequestingConsent);
        log.debug("PISP that created the payment consent in RS-STORE => '{}'", clientIdThatCreatedPayment);
        if (!clientIdRequestingConsent.equals(clientIdThatCreatedPayment)) {
            log.error("The PISP with client id: '{}' created this payment request '{}' but it's a PISP with client id" +
                            "'{}' that is trying to get consent for it.", clientIdThatCreatedPayment, consentId,
                    clientIdRequestingConsent);
            throw new OBErrorException(RCS_CONSENT_REQUEST_INVALID_PAYMENT_REQUEST, clientIdThatCreatedPayment,
                    consentId, clientIdRequestingConsent);
        }
    }
}
