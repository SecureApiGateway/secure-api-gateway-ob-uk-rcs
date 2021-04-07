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
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticPaymentConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRDomesticPaymentConsent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationRemittanceInformation;

import java.util.List;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.FRAmountConverter.toFRAmount;

@Service
@Slf4j
public class DomesticPaymentConsentDetailsService extends PaymentConsentDetailsService<FRDomesticPaymentConsent> {

    private final PaymentConsentService paymentConsentService;

    public DomesticPaymentConsentDetailsService(PaymentConsentService paymentConsentService,
                                                TppService tppService) {
        super(paymentConsentService, tppService);
        this.paymentConsentService = paymentConsentService;
    }

    @Override
    protected FRDomesticPaymentConsent getConsent(String consentId) {
        log.debug("Retrieving domestic payment consent with ID {}", consentId);
        return paymentConsentService.getConsent(consentId, FRDomesticPaymentConsent.class);
    }

    @Override
    protected String getDebitAccountId(FRDomesticPaymentConsent paymentConsent) {
        OBWriteDomestic2DataInitiation initiation = paymentConsent.getData().getInitiation();
        if (initiation.getDebtorAccount() == null) {
            return null;
        }
        return initiation.getDebtorAccount().getIdentification();
    }

    @Override
    protected ConsentDetails buildResponse(FRDomesticPaymentConsent paymentConsent,
                                           List<FRAccountWithBalance> accounts,
                                           Tpp tpp) {
        OBWriteDomestic2DataInitiation initiation = paymentConsent.getData().getInitiation();
        return DomesticPaymentConsentDetails.builder()
                .instructedAmount(toFRAmount(initiation.getInstructedAmount()))
                .accounts(accounts)
                .username(paymentConsent.getResourceOwnerUsername())
                .clientId(tpp.getClientId())
                .logo(tpp.getLogoUri())
                .merchantName(paymentConsent.getOauth2ClientName())
                .paymentReference(Optional.ofNullable(
                        initiation.getRemittanceInformation())
                        .map(OBWriteDomestic2DataInitiationRemittanceInformation::getReference)
                        .orElse(""))
                .build();
    }
}
