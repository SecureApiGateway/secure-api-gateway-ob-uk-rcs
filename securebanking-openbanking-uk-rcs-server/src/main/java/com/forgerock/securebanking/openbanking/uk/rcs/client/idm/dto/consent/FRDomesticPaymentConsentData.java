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
package com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import uk.org.openbanking.datamodel.payment.*;

import java.util.List;

/**
 * A copy of {@link OBWriteDomesticConsentResponse5Data} (with the required {@link JsonProperty} annotations, which
 * deserialize the OB formatted JSON from IDM), but using {@link FRConsentStatusCode} to allow the consent status
 * to be updated in one place.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FRDomesticPaymentConsentData implements FRPaymentConsentData {
    @JsonProperty("ConsentId")
    private String consentId;
    @JsonProperty("CreationDateTime")
    private DateTime creationDateTime;
    @JsonProperty("Status")
    private FRConsentStatusCode status;
    @JsonProperty("StatusUpdateDateTime")
    private DateTime statusUpdateDateTime;
    @JsonProperty("ReadRefundAccount")
    private OBReadRefundAccountEnum readRefundAccount;
    @JsonProperty("CutOffDateTime")
    private DateTime cutOffDateTime;
    @JsonProperty("ExpectedExecutionDateTime")
    private DateTime expectedExecutionDateTime;
    @JsonProperty("ExpectedSettlementDateTime")
    private DateTime expectedSettlementDateTime;
    @JsonProperty("Charges")
    private List<OBWriteDomesticConsentResponse5DataCharges> charges;
    @JsonProperty("Initiation")
    private OBWriteDomestic2DataInitiation initiation;
    @JsonProperty("Authorisation")
    private OBWriteDomesticConsent4DataAuthorisation authorisation;
    @JsonProperty("SCASupportData")
    private OBWriteDomesticConsent4DataSCASupportData scASupportData;
    @JsonProperty("Debtor")
    private OBDebtorIdentification1 debtor;
}
