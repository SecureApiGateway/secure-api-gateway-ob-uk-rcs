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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.customerinfo;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.BaseConsentDecisionService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.customerinfo.CustomerInfoConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo.CustomerInfoAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo.CustomerInfoConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Component
public class CustomerInfoConsentDecisionService extends BaseConsentDecisionService<CustomerInfoConsentEntity, CustomerInfoAuthoriseConsentArgs> {

    public CustomerInfoConsentDecisionService(CustomerInfoConsentService customerInfoConsentService) {
        super(IntentType.CUSTOMER_INFO_CONSENT, customerInfoConsentService);
    }

    @Override
    protected CustomerInfoAuthoriseConsentArgs buildAuthoriseConsentArgs(String intentId, String apiClientId, String resourceOwnerId, ConsentDecisionDeserialized consentDecision) {
        return new CustomerInfoAuthoriseConsentArgs(intentId, apiClientId, resourceOwnerId);
    }
}
