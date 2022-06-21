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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.forgerock.securebanking.platform.client.IntentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Interface for each type of consent data.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        // Required to deserialise the RCS ConsentDetails response into the correct types in RS-API
        @Type(value = DomesticPaymentsConsentDetails.class, name = "DomesticPaymentsConsentDetails"),
        @Type(value = DomesticScheduledPaymentsConsentDetails.class, name = "DomesticScheduledPaymentConsentDetails"),
        @Type(value = DomesticStandingOrderPaymentsConsentDetails.class, name = "DomesticStandingOrderConsentDetails"),
        @Type(value = InternationalPaymentConsentDetails.class, name = "InternationalPaymentConsentDetails"),
        @Type(value = InternationalScheduledPaymentConsentDetails.class, name = "InternationalScheduledPaymentConsentDetails"),
        @Type(value = InternationalStandingOrderConsentDetails.class, name = "InternationalStandingOrderConsentDetails"),
        @Type(value = FilePaymentConsentDetails.class, name = "FilePaymentConsentDetails")
})
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class ConsentDetails {

    private String decisionApiUri;
    private String username;
    private String userId;
    private String logo;
    private String clientId;

    public abstract IntentType getIntentType();

    public String getDecisionApiUri() {
        return "/api/rcs/consent/decision/";
    }
}
