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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticStandingOrderConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRDomesticStandingOrderConsent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrder3DataInitiation;

import java.util.List;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.FRAccountIdentifierConverter.toFRAccountIdentifier;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.FRAmountConverter.toFRAmount;

@Service
@Slf4j
public class DomesticStandingOrderConsentDetailsService extends PaymentConsentDetailsService<FRDomesticStandingOrderConsent> {

    private final PaymentConsentService paymentConsentService;

    public DomesticStandingOrderConsentDetailsService(PaymentConsentService paymentConsentService, TppService tppService) {
        super(paymentConsentService, tppService);
        this.paymentConsentService = paymentConsentService;
    }

    @Override
    protected FRDomesticStandingOrderConsent getConsent(String consentId) {
        log.debug("Retrieving domestic standing order consent with ID {}", consentId);
        return paymentConsentService.getConsent(consentId, FRDomesticStandingOrderConsent.class);
    }

    @Override
    protected String getDebitAccountId(FRDomesticStandingOrderConsent paymentConsent) {
        OBWriteDomesticStandingOrder3DataInitiation initiation = paymentConsent.getData().getInitiation();
        if (initiation.getDebtorAccount() == null) {
            return null;
        }
        return initiation.getDebtorAccount().getIdentification();
    }

    @Override
    protected ConsentDetails buildResponse(FRDomesticStandingOrderConsent paymentConsent,
                                           List<FRAccountWithBalance> accounts,
                                           Tpp tpp) {
        OBWriteDomesticStandingOrder3DataInitiation initiation = paymentConsent.getData().getInitiation();
        FRStandingOrderData standingOrderData = FRStandingOrderData.builder()
                .accountId(paymentConsent.getAccountId())
                .standingOrderId(paymentConsent.getId())
                .finalPaymentAmount(toFRAmount(initiation.getFinalPaymentAmount()))
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .firstPaymentAmount(toFRAmount(initiation.getFirstPaymentAmount()))
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .nextPaymentDateTime(initiation.getRecurringPaymentDateTime())
                .nextPaymentAmount(toFRAmount(initiation.getRecurringPaymentAmount()))
                .frequency(initiation.getFrequency())
                .creditorAccount(toFRAccountIdentifier(initiation.getCreditorAccount()))
                .reference(initiation.getReference())
                .build();
        return DomesticStandingOrderConsentDetails.builder()
                .standingOrder(standingOrderData)
                .accounts(accounts)
                .username(paymentConsent.getResourceOwnerUsername())
                .clientId(tpp.getClientId())
                .logo(tpp.getLogoUri())
                .merchantName(paymentConsent.getOauth2ClientName())
                .paymentReference(Optional.ofNullable(initiation.getReference()).orElse(""))
                .build();
    }
}