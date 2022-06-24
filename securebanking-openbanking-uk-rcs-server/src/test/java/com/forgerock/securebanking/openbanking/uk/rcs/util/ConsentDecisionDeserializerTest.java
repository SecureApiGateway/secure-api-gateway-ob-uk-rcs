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
package com.forgerock.securebanking.openbanking.uk.rcs.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.ConsentDecisionRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ConsentDecisionDeserializer}
 */
public class ConsentDecisionDeserializerTest {

    @Test
    public void shouldDeserializeConsent() throws OBErrorException {

        ConsentDecisionRequest result = ConsentDecisionDeserializer.deserializeConsentDecision(
                aValidConsentDecisionSerialised(),
                new ObjectMapper(),
                ConsentDecisionRequest.class);

        assertThat(result.getAccountIds()).isNotNull();
        assertThat(result.getAccountIds().size()).isEqualTo(3);
    }

    private String aValidConsentDecisionSerialised() {
        return "{\"consentJwt\":\"afibelaDFDFCaoehfldXXX\"," +
                "\"decision\":\"Authorised\"," +
                "\"accountIds\":[\"ddb08e74-e22a-4012-99f3-154ba52eb0eb\"," +
                "\"1dfa82b8-7f95-4d6a-a29f-de3244b2bafd\",\"73b579ec-6eca-4212-a972-602d30d62b5c\"]}";
    }
}
