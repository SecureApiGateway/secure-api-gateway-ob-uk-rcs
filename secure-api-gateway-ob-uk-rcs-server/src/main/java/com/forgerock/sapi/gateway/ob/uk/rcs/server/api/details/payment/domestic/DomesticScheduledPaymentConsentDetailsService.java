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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticScheduledConsentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticScheduledDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticScheduledPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticScheduledPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Component
@DependsOn({"internalConsentServices"})
public class DomesticScheduledPaymentConsentDetailsService extends BasePaymentConsentDetailsService<DomesticScheduledPaymentConsentEntity, DomesticScheduledPaymentConsentDetails> {

    public DomesticScheduledPaymentConsentDetailsService(
            @Qualifier("internalDomesticScheduledPaymentConsentService") ConsentService<DomesticScheduledPaymentConsentEntity, ?> consentService,
            ApiProviderConfiguration apiProviderConfiguration,
            ApiClientServiceClient apiClientService,
            AccountService accountService) {

        super(IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT, DomesticScheduledPaymentConsentDetails::new, consentService,
              apiProviderConfiguration, apiClientService, accountService);
    }

    @Override
    protected void addIntentTypeSpecificData(DomesticScheduledPaymentConsentDetails consentDetails, DomesticScheduledPaymentConsentEntity consent, ConsentClientDetailsRequest consentClientDetailsRequest) {
        final FRAmount totalChargeAmount = computeTotalChargeAmount(consent.getCharges());
        consentDetails.setCharges(totalChargeAmount);

        final FRWriteDomesticScheduledConsentData obConsentRequestData = consent.getRequestObj().getData();
        final FRWriteDomesticScheduledDataInitiation initiation = obConsentRequestData.getInitiation();
        consentDetails.setInitiation(initiation);
        consentDetails.setInstructedAmount(initiation.getInstructedAmount());
        if (initiation.getRemittanceInformation() != null) {
            consentDetails.setPaymentReference(initiation.getRemittanceInformation().getReference());
        }
        consentDetails.setPaymentDate(initiation.getRequestedExecutionDateTime());

        addDebtorAccountDetails(consentDetails);
    }
}
