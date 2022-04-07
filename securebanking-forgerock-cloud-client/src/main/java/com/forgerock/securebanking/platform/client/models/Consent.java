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
package com.forgerock.securebanking.platform.client.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.forgerock.securebanking.platform.client.IntentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Abstract class for each type of consent data.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class Consent {

    private String id;
    private String resourceOwnerUsername;
    private String oauth2ClientId;
    private String oauth2ClientName;

    public abstract IntentType getIntentType();

    public String getDecisionApiUri() {
        return "/api/rcs/consent/decision/";
    }
}
