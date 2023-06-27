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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.international;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduledConsentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduledDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.InternationalScheduledPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment.BasePaymentConsentDetailsService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.InternationalScheduledPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Component
public class InternationalScheduledPaymentConsentDetailsService extends BasePaymentConsentDetailsService<InternationalScheduledPaymentConsentEntity, InternationalScheduledPaymentConsentDetails> {

    public InternationalScheduledPaymentConsentDetailsService(ConsentService<InternationalScheduledPaymentConsentEntity, ?> consentService,
                                                              ApiProviderConfiguration apiProviderConfiguration, ApiClientServiceClient apiClientService,
                                                              AccountService accountService) {

        super(IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT, InternationalScheduledPaymentConsentDetails::new, consentService,
                apiProviderConfiguration, apiClientService, accountService);
    }

    @Override
    protected void addIntentTypeSpecificData(InternationalScheduledPaymentConsentDetails consentDetails, InternationalScheduledPaymentConsentEntity consent,
                                             ConsentClientDetailsRequest consentClientDetailsRequest) {
        final FRAmount totalChargeAmount = computeTotalChargeAmount(consent.getCharges());
        consentDetails.setCharges(totalChargeAmount);

        final FRWriteInternationalScheduledConsentData obConsentRequestData = consent.getRequestObj().getData();
        final FRWriteInternationalScheduledDataInitiation initiation = obConsentRequestData.getInitiation();
        consentDetails.setInitiation(initiation);
        consentDetails.setInstructedAmount(initiation.getInstructedAmount());
        if (initiation.getRemittanceInformation() != null) {
            consentDetails.setPaymentReference(initiation.getRemittanceInformation().getReference());
        }
        consentDetails.setPaymentDate(initiation.getRequestedExecutionDateTime());
        consentDetails.setExchangeRateInformation(consent.getExchangeRateInformation());
        consentDetails.setCurrencyOfTransfer(initiation.getCurrencyOfTransfer());

        addDebtorAccountDetails(consentDetails);
    }

}
