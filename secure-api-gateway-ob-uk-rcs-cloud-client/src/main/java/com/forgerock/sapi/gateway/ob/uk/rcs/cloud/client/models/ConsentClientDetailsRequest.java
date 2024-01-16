/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models;

import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the required information to provide the details of a consent.
 */
@Data
@Builder
@AllArgsConstructor
public class ConsentClientDetailsRequest {
    public String intentId;
    protected SignedJWT consentRequestJwt;
    protected User user;
    protected String clientId;

    public String getConsentRequestJwtString() {
        return consentRequestJwt.getParsedString();
    }
}
