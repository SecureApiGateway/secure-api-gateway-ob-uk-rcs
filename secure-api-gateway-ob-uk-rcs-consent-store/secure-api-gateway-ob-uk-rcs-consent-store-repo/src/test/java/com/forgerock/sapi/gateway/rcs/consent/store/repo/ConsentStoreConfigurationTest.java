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
package com.forgerock.sapi.gateway.rcs.consent.store.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"consent.store.enabled.intentTypes= PAYMENT_DOMESTIC_CONSENT, ACCOUNT_ACCESS_CONSENT"})
class ConsentStoreConfigurationTest {

    @Autowired
    private EnumSet<IntentType> enabledIntentTypes;

    @Test
    void testIntentTypeEnabledConfig() {
        assertThat(enabledIntentTypes).isNotNull().containsExactly(IntentType.ACCOUNT_ACCESS_CONSENT, IntentType.PAYMENT_DOMESTIC_CONSENT);
    }

}