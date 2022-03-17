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
package com.forgerock.securebanking.platform.client.models.domestic.payments;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRDataAuthorisation;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticDataInitiation;
import com.forgerock.securebanking.platform.client.services.general.ConsentServiceInterface;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.flogger.Flogger;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomesticPaymentConsentDataDetails implements DomesticPaymentConsentData {
    @JsonProperty("ConsentId")
    private String consentId;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("CreationDateTime")
    private DateTime creationDateTime;
    @JsonProperty("StatusUpdateDateTime")
    private DateTime statusUpdateDateTime;
    @JsonProperty("CutOffDateTime")
    private DateTime cutOffDateTime;
    @JsonProperty("ExpectedExecutionDateTime")
    private DateTime expectedExecutionDateTime;
    @JsonProperty("ExpectedSettlementDateTime")
    private DateTime expectedSettlementDateTime;
    @JsonProperty("Charges")
    private List<String> charges;
    @JsonProperty("Initiation")
    private FRWriteDomesticDataInitiation initiation;
    @JsonProperty("Authorisation")
    private FRDataAuthorisation authorisation;
}
