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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.DebtorAccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.BaseConsentDetailsService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.google.common.annotations.VisibleForTesting;

import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4Data;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5DataCharges;

@Component
public class DomesticPaymentConsentDetailsService extends BaseConsentDetailsService<DomesticPaymentConsentEntity, DomesticPaymentConsentDetails> {

    private final DebtorAccountService debtorAccountService;
    private final AccountService accountService;

    public DomesticPaymentConsentDetailsService(ConsentService<DomesticPaymentConsentEntity, ?> consentService,
            ApiProviderConfiguration apiProviderConfiguration, ApiClientServiceClient apiClientService,
            DebtorAccountService debtorAccountService, AccountService accountService) {

        super(IntentType.PAYMENT_DOMESTIC_CONSENT, consentService, apiProviderConfiguration, apiClientService);
        this.debtorAccountService = debtorAccountService;
        this.accountService = accountService;
    }

    @Override
    protected DomesticPaymentConsentDetails createConsentDetailsObject() {
        return new DomesticPaymentConsentDetails();
    }

    @Override
    protected void addIntentTypeSpecificData(DomesticPaymentConsentDetails consentDetails, DomesticPaymentConsentEntity consent,
                                             ConsentClientDetailsRequest consentClientDetailsRequest) {
        final FRAmount totalChargeAmount = computeTotalChargeAmount(consent.getCharges());
        consentDetails.setCharges(totalChargeAmount);

        final OBWriteDomesticConsent4Data obConsentRequestData = consent.getRequestObj().getData();
        final FRWriteDomesticDataInitiation initiation = FRWriteDomesticConsentConverter.toFRWriteDomesticDataInitiation(obConsentRequestData.getInitiation());
        consentDetails.setInitiation(initiation);
        consentDetails.setInstructedAmount(initiation.getInstructedAmount());
        if (initiation.getRemittanceInformation() != null) {
            consentDetails.setPaymentReference(initiation.getRemittanceInformation().getReference());
        }

        if (Objects.nonNull(consentDetails.getDebtorAccount())) {
            debtorAccountService.setDebtorAccountWithBalance(consentDetails, consentClientDetailsRequest.getConsentRequestJwtString(), consentDetails.getConsentId());
        } else {
            consentDetails.setAccounts(accountService.getAccountsWithBalance(consentDetails.getUserId()));
        }
    }

    @VisibleForTesting
    static FRAmount computeTotalChargeAmount(List<OBWriteDomesticConsentResponse5DataCharges> charges) {
        String chargeCurrency = null;
        BigDecimal totalCharge = BigDecimal.ZERO;
        for (OBWriteDomesticConsentResponse5DataCharges charge : charges) {
            final OBActiveOrHistoricCurrencyAndAmount amount = charge.getAmount();
            if (chargeCurrency == null) {
                chargeCurrency = amount.getCurrency();
            } else if (!chargeCurrency.equals(amount.getCurrency())) {
                throw new IllegalStateException("Charges contain more than 1 currency, all charges must be in the same currency");
            }
            totalCharge = totalCharge.add(new BigDecimal(amount.getAmount()));
        }
        return new FRAmount(totalCharge.toPlainString(), chargeCurrency);
    }

}
