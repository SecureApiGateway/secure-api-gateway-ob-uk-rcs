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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.file.v4_0_0;

import com.forgerock.sapi.gateway.rcs.consent.store.api.payment.file.BaseFilePaymentConsentApiController;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.file.FilePaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import io.swagger.annotations.Api;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.function.Supplier;

/**
 * Implementation of FilePaymentConsentApi for OBIE version 4.0.0
 */
@Controller("V4.0.0FilePaymentConsentApiController")
@Api(tags = {"v4.0.0"})
@RequestMapping(value = "/consent/store/v4.0.0")
@DependsOn({"versionedConsentServices"})
public class FilePaymentConsentApiController extends BaseFilePaymentConsentApiController {

    @Autowired
    public FilePaymentConsentApiController(
            @Qualifier("v4.0.0FilePaymentConsentService") FilePaymentConsentService consentService,
            Supplier<DateTime> idempotencyKeyExpirationSupplier) {

        super(consentService, idempotencyKeyExpirationSupplier, OBVersion.v4_0_0);
    }

}
