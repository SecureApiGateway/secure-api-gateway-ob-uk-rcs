/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details;

import org.joda.time.DateTime;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Models the consent data for a funds confirmation request.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class FundsConfirmationConsentDetails extends ConsentDetails {

    private DateTime expirationDateTime;
    private FRAccountIdentifier debtorAccount;

    @Override
    public IntentType getIntentType() {
        return IntentType.FUNDS_CONFIRMATION_CONSENT;
    }

}
