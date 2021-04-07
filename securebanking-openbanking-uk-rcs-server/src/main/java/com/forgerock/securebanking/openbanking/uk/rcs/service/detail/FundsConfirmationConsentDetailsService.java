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
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.FundsConfirmationConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRFundsConfirmationConsent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.account.OBCashAccount3;

import java.util.List;

@Service
@Slf4j
public class FundsConfirmationConsentDetailsService extends PaymentConsentDetailsService<FRFundsConfirmationConsent> {

    private final PaymentConsentService paymentConsentService;

    public FundsConfirmationConsentDetailsService(PaymentConsentService paymentConsentService, TppService tppService) {
        super(paymentConsentService, tppService);
        this.paymentConsentService = paymentConsentService;
    }

    @Override
    protected FRFundsConfirmationConsent getConsent(String consentId) {
        log.debug("Retrieving funds confirmation consent with ID {}", consentId);
        return paymentConsentService.getConsent(consentId, FRFundsConfirmationConsent.class);
    }

    @Override
    protected String getDebitAccountId(FRFundsConfirmationConsent consent) {
        OBCashAccount3 debtorAccount = consent.getData().getDebtorAccount();
        if (debtorAccount == null) {
            return null;
        }
        return debtorAccount.getIdentification();
    }

    @Override
    protected ConsentDetails buildResponse(FRFundsConfirmationConsent consent,
                                           List<FRAccountWithBalance> accounts,
                                           Tpp tpp) {
        return FundsConfirmationConsentDetails.builder()
                .expirationDateTime(consent.getData().getExpirationDateTime())
                .accounts(accounts)
                .username(consent.getResourceOwnerUsername())
                .clientId(tpp.getClientId())
                .logo(tpp.getLogoUri())
                .merchantName(consent.getOauth2ClientName())
                .build();
    }
}