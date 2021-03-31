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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FRDomesticStandingOrderConsentData implements FRPaymentConsentData {
    @JsonProperty("ConsentId")
    private String consentId = null;
    @JsonProperty("CreationDateTime")
    private DateTime creationDateTime = null;
    @JsonProperty("Status")
    private FRConsentStatusCode status = null;
    @JsonProperty("StatusUpdateDateTime")
    private DateTime statusUpdateDateTime = null;
    @JsonProperty("Permission")
    private OBWriteDomesticStandingOrderConsentResponse6Data.PermissionEnum permission = null;
    @JsonProperty("ReadRefundAccount")
    private OBWriteDomesticStandingOrderConsentResponse6Data.ReadRefundAccountEnum readRefundAccount = null;
    @JsonProperty("CutOffDateTime")
    private DateTime cutOffDateTime = null;
    @JsonProperty("Charges")
    private List<OBWriteDomesticConsentResponse5DataCharges> charges = null;
    @JsonProperty("Initiation")
    private OBWriteDomesticStandingOrder3DataInitiation initiation = null;
    @JsonProperty("Authorisation")
    private OBWriteDomesticConsent4DataAuthorisation authorisation = null;
    @JsonProperty("SCASupportData")
    private OBWriteDomesticConsent4DataSCASupportData scASupportData = null;
    @JsonProperty("Debtor")
    private OBDebtorIdentification1 debtor = null;
}
