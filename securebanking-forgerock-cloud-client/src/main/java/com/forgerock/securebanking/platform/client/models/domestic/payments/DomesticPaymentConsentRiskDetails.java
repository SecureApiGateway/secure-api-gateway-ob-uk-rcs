/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.securebanking.platform.client.models.domestic.payments;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticDataInitiation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomesticPaymentConsentRiskDetails{
    @JsonProperty("ConsentId")
    private String consentId;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("CreationDateTime")
    private DateTime creationDateTime;
    @JsonProperty("StatusUpdateDateTime")
    private DateTime statusUpdateDateTime;
    @JsonProperty("Initiation")
    private FRWriteDomesticDataInitiation initiation;
}
