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
package com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduledDataInitiation;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joda.time.DateTime;

/**
 * Models the consent data for an international scheduled payment.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class InternationalScheduledPaymentConsentDetails extends PaymentsConsentDetails {
    private FRWriteInternationalScheduledDataInitiation initiation;
    private FRAmount instructedAmount;
    private FRAmount charges;
    private FRExchangeRateInformation exchangeRateInformation;
    private DateTime paymentDate;
    private String currencyOfTransfer;
    private String paymentReference;

    @Override
    public FRAccountIdentifier getDebtorAccount() {
        return initiation.getDebtorAccount();
    }
    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT;
    }
}
