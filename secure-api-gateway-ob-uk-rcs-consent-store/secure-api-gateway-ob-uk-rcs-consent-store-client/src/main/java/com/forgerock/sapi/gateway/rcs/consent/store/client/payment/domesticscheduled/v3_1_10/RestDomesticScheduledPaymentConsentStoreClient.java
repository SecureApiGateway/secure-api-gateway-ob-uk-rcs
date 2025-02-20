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
package com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticscheduled.v3_1_10;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticscheduled.BaseRestDomesticScheduledPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

/**
 * v3.1.10 implementation of RestDomesticScheduledPaymentConsentStoreClient
 */
@Component("v3.1.10RestDomesticScheduledPaymentConsentStoreClient")
public class RestDomesticScheduledPaymentConsentStoreClient extends BaseRestDomesticScheduledPaymentConsentStoreClient {

    public RestDomesticScheduledPaymentConsentStoreClient(
            ConsentStoreClientConfiguration consentStoreClientConfiguration,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper) {

        super(consentStoreClientConfiguration, restTemplateBuilder, objectMapper, OBVersion.v3_1_10);
    }
}
