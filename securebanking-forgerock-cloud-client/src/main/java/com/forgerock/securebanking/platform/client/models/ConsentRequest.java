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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents the required information to provide the details of a consent.
 */
@Data
@Builder
@AllArgsConstructor
public class ConsentRequest {
    public String intentId;
    protected SignedJWT consentRequestJwt;
    protected User user;
    protected String clientId;
    List<FRAccountWithBalance> accounts;

    public List<FRAccountWithBalance> getAccounts() {
        return accounts;
    }

    public String getConsentRequestJwtString() {
        return consentRequestJwt.getParsedString();
    }
}
