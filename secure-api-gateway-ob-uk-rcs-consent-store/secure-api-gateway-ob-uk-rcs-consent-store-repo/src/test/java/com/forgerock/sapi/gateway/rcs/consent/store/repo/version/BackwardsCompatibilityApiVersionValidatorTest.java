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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

class BackwardsCompatibilityApiVersionValidatorTest {

    private final BackwardsCompatibilityApiVersionValidator apiVersionValidator = new BackwardsCompatibilityApiVersionValidator();

    @Test
    void shouldAllowConsentToBeAccessedUsingSameApiVersion() {
        assertTrue(apiVersionValidator.canAccessResourceUsingApiVersion(OBVersion.v3_1_10, OBVersion.v3_1_10));
    }

    @Test
    void shouldAllowConsentToBeAccessedFromFutureApiVersion() {
        assertTrue(apiVersionValidator.canAccessResourceUsingApiVersion(OBVersion.v3_1_10, OBVersion.v4_0_0));
    }

    @Test
    void shouldFailToAccessConsentFromOlderApiVersion() {
        assertFalse(apiVersionValidator.canAccessResourceUsingApiVersion(OBVersion.v4_0_0, OBVersion.v3_1_10));
    }

    @Test
    void shouldFailIfParamsAreNull() {
        NullPointerException npe = assertThrows(NullPointerException.class,
                () -> apiVersionValidator.canAccessResourceUsingApiVersion(null, null));
        assertThat(npe).hasMessage("creationVersion must be provided");

        npe = assertThrows(NullPointerException.class,
                () -> apiVersionValidator.canAccessResourceUsingApiVersion(OBVersion.v4_0_0, null));
        assertThat(npe).hasMessage("accessVersion must be provided");
    }

}