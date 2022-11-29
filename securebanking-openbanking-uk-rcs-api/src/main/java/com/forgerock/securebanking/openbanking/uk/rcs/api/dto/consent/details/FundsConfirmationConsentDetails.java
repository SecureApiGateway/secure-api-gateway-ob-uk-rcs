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
package com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details;

import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * Models the consent data for a confirmation of funds request.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Component
public class FundsConfirmationConsentDetails extends ConsentDetails {

    private DateTime expirationDateTime;

    @Override
    public IntentType getIntentType() {
        return IntentType.FUNDS_CONFIRMATION_CONSENT;
    }

    @Override
    public void mapping(JsonObject consent) {
        log.warn("NOT IMPLEMENTED YET");
    }

}
