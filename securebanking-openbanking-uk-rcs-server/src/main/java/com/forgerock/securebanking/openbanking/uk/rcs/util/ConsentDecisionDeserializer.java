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
package com.forgerock.securebanking.openbanking.uk.rcs.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_DECISIONS_FORMAT;

public class ConsentDecisionDeserializer {

    public static <T> T deserializeConsentDecision(
            String consentDecisionSerialised,
            ObjectMapper objectMapper,
            Class<T> consentClass
    ) throws OBErrorException {
        try {
            return objectMapper.readValue(consentDecisionSerialised, consentClass);
        } catch (JsonProcessingException e) {
            throw new OBErrorException(RCS_CONSENT_DECISIONS_FORMAT, e.getMessage());
        }
    }
}
