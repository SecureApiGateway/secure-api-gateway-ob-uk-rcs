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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public enum IntentType {

    ACCOUNT_REQUEST("AR_"),
    ACCOUNT_ACCESS_CONSENT("AAC_"),
    PAYMENT_DOMESTIC_CONSENT("PDC_"),
    PAYMENT_DOMESTIC_SCHEDULED_CONSENT("PDSC_"),
    PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT("PDSOC_"),
    PAYMENT_INTERNATIONAL_CONSENT("PIC_"),
    PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT("PISC_"),
    PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT("PISOC_"),
    PAYMENT_FILE_CONSENT("PFC_"),
    FUNDS_CONFIRMATION_CONSENT("FCC_"),
    DOMESTIC_VRP_PAYMENT_CONSENT("DVRP_");
    private String intentIdPrefix;

    IntentType(String intentIdPrefix) {
        this.intentIdPrefix = intentIdPrefix;
    }

    public static IntentType identify(String intentId) {
        IntentType[] types = values();
        Optional<IntentType> optional = Arrays.stream(types).filter(type -> intentId.startsWith(type.intentIdPrefix)).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public String generateIntentId() {
        String intentId = this.intentIdPrefix + UUID.randomUUID();
        return intentId.substring(0, Math.min(intentId.length(), 40));
    }

    public String getIntentIdPrefix() {
        return intentIdPrefix;
    }
}
