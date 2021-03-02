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
package com.forgerock.securebanking.openbanking.uk.rcs.client.idm;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRPaymentConsent;

/**
 * An internal Payment service
 * @param <T> A type of payment consent
 */
public interface PaymentConsentService<T extends FRPaymentConsent> {

    /**
     * Get payment consent by id
     * @param paymentId Payment id
     * @return Payment consent
     */
    T getConsent(String paymentId);

    /**
     * Update payment consent
     * @param payment Payment consent
     */
    void updateConsent(T payment);

}
