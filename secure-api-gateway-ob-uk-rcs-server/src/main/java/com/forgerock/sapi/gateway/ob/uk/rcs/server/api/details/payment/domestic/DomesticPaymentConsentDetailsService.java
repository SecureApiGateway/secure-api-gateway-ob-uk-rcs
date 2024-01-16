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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.domestic;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticConsentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Component
public class DomesticPaymentConsentDetailsService extends BasePaymentConsentDetailsService<DomesticPaymentConsentEntity, DomesticPaymentConsentDetails> {

    public DomesticPaymentConsentDetailsService(ConsentService<DomesticPaymentConsentEntity, ?> consentService,
            ApiProviderConfiguration apiProviderConfiguration, ApiClientServiceClient apiClientService,
            AccountService accountService) {

        super(IntentType.PAYMENT_DOMESTIC_CONSENT, DomesticPaymentConsentDetails::new, consentService,
                apiProviderConfiguration, apiClientService, accountService);
    }

    @Override
    protected void addIntentTypeSpecificData(DomesticPaymentConsentDetails consentDetails, DomesticPaymentConsentEntity consent,
                                             ConsentClientDetailsRequest consentClientDetailsRequest) {
        final FRAmount totalChargeAmount = computeTotalChargeAmount(consent.getCharges());
        consentDetails.setCharges(totalChargeAmount);

        final FRWriteDomesticConsentData obConsentRequestData = consent.getRequestObj().getData();
        final FRWriteDomesticDataInitiation initiation = obConsentRequestData.getInitiation();
        consentDetails.setInitiation(initiation);
        consentDetails.setInstructedAmount(initiation.getInstructedAmount());
        if (initiation.getRemittanceInformation() != null) {
            consentDetails.setPaymentReference(initiation.getRemittanceInformation().getReference());
        }

        addDebtorAccountDetails(consentDetails);
    }

}
