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
package com.forgerock.sapi.gateway.rcs.consent.store.client.funds.v4_0_0;

import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.BaseFundsConfirmationConsentStoreClientTest;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.BaseRestFundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.FundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.v3_1_10.RestFundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import static com.forgerock.sapi.gateway.rcs.consent.store.client.TestConsentStoreClientConfigurationFactory.createConsentStoreClientConfiguration;

/**
 * Test for {@link FundsConfirmationConsentStoreClient}
 */

class FundsConfirmationConsentStoreClientTest extends BaseFundsConfirmationConsentStoreClientTest {

    protected FundsConfirmationConsentStoreClientTest() {
        super(OBVersion.v4_0_0);
    }

    @Override
    protected BaseRestFundsConfirmationConsentStoreClient createApiClient() {
        return new RestFundsConfirmationConsentStoreClient(createConsentStoreClientConfiguration(port),
                restTemplateBuilder,
                objectMapper);
    }

}
