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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.customerinfo;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.CustomerInfoConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.BaseConsentDetailsService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.CustomerInfoService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.customerinfo.CustomerInfoConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerInfoConsentDetailsService extends BaseConsentDetailsService<CustomerInfoConsentEntity, CustomerInfoConsentDetails> {

    private final CustomerInfoService customerInfoService;

    public CustomerInfoConsentDetailsService(
            ConsentService<CustomerInfoConsentEntity, ?> consentService,
            ApiProviderConfiguration apiProviderConfiguration,
            ApiClientServiceClient apiClientService,
            CustomerInfoService customerInfoService
    ) {
        super(IntentType.CUSTOMER_INFO_CONSENT, CustomerInfoConsentDetails::new, consentService,
                apiProviderConfiguration, apiClientService);
        this.customerInfoService = customerInfoService;
    }

    @Override
    protected void addIntentTypeSpecificData(
            CustomerInfoConsentDetails consentDetails,
            CustomerInfoConsentEntity consent,
            ConsentClientDetailsRequest consentClientDetailsRequest
    ) {
        consentDetails.setPermissions(consent.getRequestObj().getData().getPermissions());
        Optional<FRCustomerInfo> customerInfo = customerInfoService.getCustomerInformation(
                consentClientDetailsRequest.getUser().getId()
        );
        if (customerInfo.isPresent()) {
            consentDetails.setCustomerInfo(customerInfo.get());
        }
    }
}
