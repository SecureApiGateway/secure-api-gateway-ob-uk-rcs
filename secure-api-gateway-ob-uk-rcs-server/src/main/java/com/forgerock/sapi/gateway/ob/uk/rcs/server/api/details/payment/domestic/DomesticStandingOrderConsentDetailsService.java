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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.domestic;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticStandingOrderConsentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticStandingOrderDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticStandingOrderConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticStandingOrderConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Component
public class DomesticStandingOrderConsentDetailsService extends BasePaymentConsentDetailsService<DomesticStandingOrderConsentEntity, DomesticStandingOrderConsentDetails> {

    public DomesticStandingOrderConsentDetailsService(ConsentService<DomesticStandingOrderConsentEntity, ?> consentService, ApiProviderConfiguration apiProviderConfiguration, ApiClientServiceClient apiClientService, AccountService accountService) {
        super(IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT, DomesticStandingOrderConsentDetails::new, consentService,
              apiProviderConfiguration, apiClientService, accountService);
    }

    @Override
    protected void addIntentTypeSpecificData(DomesticStandingOrderConsentDetails consentDetails, DomesticStandingOrderConsentEntity consent, ConsentClientDetailsRequest consentClientDetailsRequest) {
        final FRAmount totalChargeAmount = computeTotalChargeAmount(consent.getCharges());
        consentDetails.setCharges(totalChargeAmount);

        final FRWriteDomesticStandingOrderConsentData obConsentRequestData = consent.getRequestObj().getData();
        final FRWriteDomesticStandingOrderDataInitiation initiation = obConsentRequestData.getInitiation();
        consentDetails.setInitiation(initiation);
        consentDetails.setPaymentReference(initiation.getReference());

        addDebtorAccountDetails(consentDetails);
    }
}
