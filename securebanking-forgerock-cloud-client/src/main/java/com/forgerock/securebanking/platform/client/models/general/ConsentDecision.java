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
package com.forgerock.securebanking.platform.client.models.general;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentDecision;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentDecision;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Abstract class for each type of consent decision data.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccountConsentDecision.class, name = "AccountConsentDecision"),
        @JsonSubTypes.Type(value = DomesticPaymentConsentDecision.class, name = "DomesticPaymentDecision")
})
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class ConsentDecision {
    @JsonIgnore
    private String consentJwt;
    @JsonIgnore
    private String intentId;
    @JsonIgnore
    private String clientId;
    @JsonIgnore
    private List<String> scopes;
    @JsonIgnore
    private JWTClaimsSet jwtClaimsSet;

    private ConsentDecisionData data;
    private String resourceOwnerUsername;
}
