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
package com.forgerock.securebanking.openbanking.uk.rcs.factory.details;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.FundsConfirmationConsentDetails;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Funds Confirmation consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
@Slf4j
public class FundsConfirmationConsentDetailsFactory implements ConsentDetailsFactory<FundsConfirmationConsentDetails> {

    @Override
    public FundsConfirmationConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        FundsConfirmationConsentDetails details = new FundsConfirmationConsentDetails();
        log.warn("Funds Confirmation Consent Details 'decode' NOT IMPLEMENTED YET");
        return details;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.FUNDS_CONFIRMATION_CONSENT;
    }
}
