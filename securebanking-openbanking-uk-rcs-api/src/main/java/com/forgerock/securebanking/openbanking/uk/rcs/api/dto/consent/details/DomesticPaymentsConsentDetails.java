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
package com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRDataAuthorisation;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticDataInitiation;
import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.services.general.ConsentServiceInterface;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Models the consent data for a domestic payment.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class DomesticPaymentsConsentDetails extends ConsentDetails {
    private DateTime cutOffDateTime;
    private DateTime expectedExecutionDateTime;
    private DateTime expectedSettlementDateTime;
    private List<String> charges;

    public void setInitiation(FRWriteDomesticDataInitiation initiation) {
        Logger log = LoggerFactory.getLogger(DomesticPaymentsConsentDetails.class);

        log.info("set DomesticPaymentsConsentDetails " + initiation.toString());
        this.initiation = initiation;
    }

    private FRWriteDomesticDataInitiation initiation;
    private FRDataAuthorisation authorisation;
    private String pispName;

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_DOMESTIC_CONSENT;
    }
}
