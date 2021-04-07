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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.InternationalPaymentConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRInternationalPaymentConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRInternationalPaymentConsentData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationRemittanceInformation;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsentResponse6DataExchangeRateInformation;

import java.util.List;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.FRAmountConverter.toFRAmount;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRExchangeRateConverter.toFRExchangeRateInformation;

@Service
@Slf4j
public class InternationalPaymentConsentDetailsService extends PaymentConsentDetailsService<FRInternationalPaymentConsent> {

    private final PaymentConsentService paymentConsentService;

    public InternationalPaymentConsentDetailsService(PaymentConsentService paymentConsentService,
                                                     TppService tppService) {
        super(paymentConsentService, tppService);
        this.paymentConsentService = paymentConsentService;
    }

    @Override
    protected FRInternationalPaymentConsent getConsent(String consentId) {
        log.debug("Retrieving international payment consent with ID {}", consentId);
        return paymentConsentService.getConsent(consentId, FRInternationalPaymentConsent.class);
    }

    @Override
    protected String getDebitAccountId(FRInternationalPaymentConsent paymentConsent) {
        OBWriteInternational3DataInitiation initiation = paymentConsent.getData().getInitiation();
        if (initiation.getDebtorAccount() == null) {
            return null;
        }
        return initiation.getDebtorAccount().getIdentification();
    }

    @Override
    protected ConsentDetails buildResponse(FRInternationalPaymentConsent paymentConsent,
                                           List<FRAccountWithBalance> accounts,
                                           Tpp tpp) {
        FRInternationalPaymentConsentData data = paymentConsent.getData();
        OBWriteInternational3DataInitiation initiation = data.getInitiation();
        OBWriteInternationalConsentResponse6DataExchangeRateInformation exchangeRate = data.getExchangeRateInformation();

        return InternationalPaymentConsentDetails.builder()
                .instructedAmount(toFRAmount(initiation.getInstructedAmount()))
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
                .rate(toFRExchangeRateInformation(exchangeRate))
                .build();
    }
}
