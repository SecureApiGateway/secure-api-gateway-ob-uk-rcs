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
package com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRBankAccountWithBalance;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.IntentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import uk.org.openbanking.datamodel.payment.OBActiveOrHistoricCurrencyAndAmount;

import java.util.List;

/**
 * Models the consent data for a file payment.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilePaymentConsentDetails extends ConsentDetails {
    protected String decisionApiUri;

    protected List<FRBankAccountWithBalance> accounts;
    protected String username;
    protected String logo;
    protected String clientId;
    protected String merchantName;
    protected String pispName;
    protected DateTime expiredDate;

    protected String fileReference;
    protected OBActiveOrHistoricCurrencyAndAmount totalAmount;
    protected String numberOfTransactions;

    protected String paymentReference;
    protected DateTime requestedExecutionDateTime;

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_FILE_CONSENT;
    }
}
