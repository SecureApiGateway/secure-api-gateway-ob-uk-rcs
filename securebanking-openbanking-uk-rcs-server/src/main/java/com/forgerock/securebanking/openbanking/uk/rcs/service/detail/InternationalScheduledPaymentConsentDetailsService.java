/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.service.detail;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRScheduledPaymentData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRExchangeRateInformation;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.InternationalScheduledPaymentConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRInternationalScheduledPaymentConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRInternationalScheduledPaymentConsentData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationRemittanceInformation;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsentResponse6DataExchangeRateInformation;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduled3DataInitiation;

import java.util.List;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.FRAccountIdentifierConverter.toFRAccountIdentifier;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.FRAmountConverter.toFRAmount;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRExchangeRateConverter.toFRRateType;

@Service
@Slf4j
public class InternationalScheduledPaymentConsentDetailsService extends PaymentConsentDetailsService<FRInternationalScheduledPaymentConsent> {

    private final PaymentConsentService paymentConsentService;

    public InternationalScheduledPaymentConsentDetailsService(PaymentConsentService paymentConsentService,
                                                              TppService tppService) {
        super(paymentConsentService, tppService);
        this.paymentConsentService = paymentConsentService;
    }

    @Override
    protected FRInternationalScheduledPaymentConsent getConsent(String consentId) {
        log.debug("Retrieving international scheduled payment consent with ID {}", consentId);
        return paymentConsentService.getConsent(consentId, FRInternationalScheduledPaymentConsent.class);
    }

    @Override
    protected String getDebitAccountId(FRInternationalScheduledPaymentConsent paymentConsent) {
        OBWriteInternationalScheduled3DataInitiation initiation = paymentConsent.getData().getInitiation();
        if (initiation.getDebtorAccount() == null) {
            return null;
        }
        return initiation.getDebtorAccount().getIdentification();
    }

    @Override
    protected ConsentDetails buildResponse(FRInternationalScheduledPaymentConsent paymentConsent,
                                           List<FRAccountWithBalance> accounts,
                                           Tpp tpp) {

        FRInternationalScheduledPaymentConsentData data = paymentConsent.getData();
        OBWriteInternationalScheduled3DataInitiation initiation = data.getInitiation();
        OBWriteInternationalConsentResponse6DataExchangeRateInformation exchangeRate = data.getExchangeRateInformation();

        FRScheduledPaymentData scheduledPaymentData = FRScheduledPaymentData.builder()
                .accountId(paymentConsent.getAccountId())
                .scheduledPaymentId(initiation.getInstructionIdentification())
                .scheduledPaymentDateTime(initiation.getRequestedExecutionDateTime())
                .creditorAccount(toFRAccountIdentifier(initiation.getCreditorAccount()))
                .instructedAmount(toFRAmount(initiation.getInstructedAmount()))
                .reference(initiation.getRemittanceInformation().getReference())
                .build();

        return InternationalScheduledPaymentConsentDetails.builder()
                .scheduledPayment(scheduledPaymentData)
                .rate(FRExchangeRateInformation.builder()
                        .exchangeRate(exchangeRate.getExchangeRate())
                        .rateType(toFRRateType(exchangeRate.getRateType()))
                        .contractIdentification(exchangeRate.getContractIdentification())
                        .unitCurrency(exchangeRate.getUnitCurrency())
                        .build())
                .accounts(accounts)
                .username(paymentConsent.getResourceOwnerUsername())
                .clientId(tpp.getClientId())
                .logo(tpp.getLogoUri())
                .merchantName(paymentConsent.getOauth2ClientName())
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .paymentReference(Optional.ofNullable(
                        initiation.getRemittanceInformation())
                        .map(OBWriteDomestic2DataInitiationRemittanceInformation::getReference)
                        .orElse(""))
                .build();
    }
}
