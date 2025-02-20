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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.international;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalStandingOrderConsentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalStandingOrderDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.InternationalStandingOrderConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.InternationalStandingOrderConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.forgerock.FRFrequency;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Component
@DependsOn({"internalConsentServices"})
public class InternationalStandingOrderConsentDetailsService extends BasePaymentConsentDetailsService<InternationalStandingOrderConsentEntity, InternationalStandingOrderConsentDetails> {

    public InternationalStandingOrderConsentDetailsService(
            @Qualifier("internalInternationalStandingOrderConsentService") ConsentService<InternationalStandingOrderConsentEntity, ?> consentService,
            ApiProviderConfiguration apiProviderConfiguration,
            ApiClientServiceClient apiClientService,
            AccountService accountService) {

        super(IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT, InternationalStandingOrderConsentDetails::new, consentService,
                apiProviderConfiguration, apiClientService, accountService);
    }

    @Override
    protected void addIntentTypeSpecificData(InternationalStandingOrderConsentDetails consentDetails,
                                             InternationalStandingOrderConsentEntity consent,
                                             ConsentClientDetailsRequest consentClientDetailsRequest) {
        final FRAmount totalChargeAmount = computeTotalChargeAmount(consent.getCharges());
        consentDetails.setCharges(totalChargeAmount);

        final FRWriteInternationalStandingOrderConsentData obConsentRequestData = consent.getRequestObj().getData();
        final FRWriteInternationalStandingOrderDataInitiation initiation = obConsentRequestData.getInitiation();

        if (initiation.getMandateRelatedInformation() != null && initiation.getMandateRelatedInformation().getFrequency() != null) {
            // Updating initiation.frequency with a readable value to be displayed in the UI
            initiation.setFrequency(initiation.getMandateRelatedInformation().getFrequency().getFormattedSentenceV4());
        } else if (initiation.getFrequency() != null) {
            FRFrequency frFrequency = new FRFrequency(initiation.getFrequency());
            initiation.setFrequency(frFrequency.getFormattedSentence());
        }

        consentDetails.setPaymentReference(initiation.getReference());
        consentDetails.setInitiation(initiation);
        consentDetails.setCurrencyOfTransfer(initiation.getCurrencyOfTransfer());

        addDebtorAccountDetails(consentDetails);
    }

}
