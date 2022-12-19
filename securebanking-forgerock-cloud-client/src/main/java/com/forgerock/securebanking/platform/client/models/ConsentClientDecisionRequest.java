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
package com.forgerock.securebanking.platform.client.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAccountIdentifier;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Abstract class for each type of consent decision data.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ConsentClientDecisionRequest {
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

    private List<String> accountIds;

    private String accountId;

    public void setDataDebtorAccount(FRAccountIdentifier accountIdentifier) {
        this.data.setDebtorAccount(accountIdentifier);
    }

    private ConsentClientDecisionRequestData data;
    private String resourceOwnerUsername;
}
