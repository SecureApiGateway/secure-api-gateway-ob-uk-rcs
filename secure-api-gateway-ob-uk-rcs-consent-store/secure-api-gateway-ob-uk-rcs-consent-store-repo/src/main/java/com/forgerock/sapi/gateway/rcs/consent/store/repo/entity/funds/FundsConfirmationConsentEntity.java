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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.funds;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.funds.FRFundsConfirmationConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.BaseConsentEntity;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

/**
 * OBIE Funds Confirmation Consent: https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/cbpii/funds-confirmation-consent.html
 */
@Document("FundsConfirmationConsent")
@Validated
public class FundsConfirmationConsentEntity extends BaseConsentEntity<FRFundsConfirmationConsent> {
    /**
     * ID of the DebtorAccount that:<br/>
     * <ul>
     *     <li>
     *         Match with the debtor account data consent
     *     </li>
     *     <li>
     *         The Resource Owner has authorised to check funds availability
     *     </li>
     * </ul>
     * This field is set as part of Consent Authorisation, therefore may be null in other states.
     */
    private String authorisedDebtorAccountId;

    public String getAuthorisedDebtorAccountId() {
        return authorisedDebtorAccountId;
    }

    public void setAuthorisedDebtorAccountId(String authorisedDebtorAccountId) {
        this.authorisedDebtorAccountId = authorisedDebtorAccountId;
    }
}
