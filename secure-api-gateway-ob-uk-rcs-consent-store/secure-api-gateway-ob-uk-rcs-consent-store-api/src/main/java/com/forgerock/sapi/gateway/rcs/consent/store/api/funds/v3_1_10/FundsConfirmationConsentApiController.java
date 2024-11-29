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
package com.forgerock.sapi.gateway.rcs.consent.store.api.funds.v3_1_10;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.forgerock.sapi.gateway.rcs.consent.store.api.funds.BaseFundsConfirmationConsentApiController;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.FundsConfirmationConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import io.swagger.annotations.Api;

@Controller("V3.1.10FundsConfirmationConsentApiController")
@Api(tags = {"v3.1.10"})
@RequestMapping(value = "/consent/store/v3.1.10")
@DependsOn({"versionedConsentServices"})
public class FundsConfirmationConsentApiController extends BaseFundsConfirmationConsentApiController {

    @Autowired
    public FundsConfirmationConsentApiController(@Qualifier("v3.1.10FundsConfirmationConsentService") FundsConfirmationConsentService consentService) {
        super(consentService, OBVersion.v3_1_10);
    }

}
