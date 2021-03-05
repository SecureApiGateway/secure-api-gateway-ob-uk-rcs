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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.IntentType;

/**
 * Interface for each type of consent data.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        // Required to deserialise the RCS ConsentDetails response into the correct types in RS-API
        @Type(value = DomesticPaymentConsentDetails.class, name = "DomesticPaymentConsentDetails"),
        @Type(value = DomesticSchedulePaymentConsentDetails.class, name = "DomesticSchedulePaymentConsentDetails"),
        @Type(value = DomesticStandingOrderPaymentConsentDetails.class, name = "DomesticStandingOrderPaymentConsentDetails"),
        @Type(value = InternationalPaymentConsentDetails.class, name = "InternationalPaymentConsentDetails"),
        @Type(value = InternationalSchedulePaymentConsentDetails.class, name = "InternationalSchedulePaymentConsentDetails"),
        @Type(value = InternationalStandingOrderPaymentConsentDetails.class, name = "InternationalStandingOrderPaymentConsentDetails"),
        @Type(value = FilePaymentConsentDetails.class, name = "FilePaymentConsentDetails")
})
public abstract class ConsentDetails {

    protected static String DECISION_API_URI = "/api/rcs/consent/decision/";

    @JsonProperty("intentType")
    public abstract IntentType getIntentType();

    @JsonProperty("decisionApiUri")
    public abstract String getDecisionApiUri();
}
