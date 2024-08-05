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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domesticstandingorder.v3_1_10;

import java.util.function.Supplier;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DomesticStandingOrderConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import io.swagger.annotations.Api;

/**
 * Implementation of DomesticStandingOrderPaymentConsentApi for OBIE version 3.1.10
 */
@Controller
@Api(tags = {"v3.1.10"})
@RequestMapping(value = "/consent/store/v3.1.10")
public class DomesticStandingOrderConsentApiController extends com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domesticstandingorder.BaseDomesticStandingOrderConsentApiController {

    @Autowired
    public DomesticStandingOrderConsentApiController(DomesticStandingOrderConsentService consentService,
                                                     Supplier<DateTime> idempotencyKeyExpirationSupplier) {
        super(consentService, idempotencyKeyExpirationSupplier, OBVersion.v3_1_10);
    }

}
