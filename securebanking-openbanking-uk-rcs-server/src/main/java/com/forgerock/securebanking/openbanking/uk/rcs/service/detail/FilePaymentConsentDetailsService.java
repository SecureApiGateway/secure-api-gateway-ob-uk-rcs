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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRAmount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.FilePaymentConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRFilePaymentConsent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationRemittanceInformation;
import uk.org.openbanking.datamodel.payment.OBWriteFile2DataInitiation;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FilePaymentConsentDetailsService extends PaymentConsentDetailsService<FRFilePaymentConsent> {

    private final PaymentConsentService paymentConsentService;

    public FilePaymentConsentDetailsService(PaymentConsentService paymentConsentService, TppService tppService) {
        super(paymentConsentService, tppService);
        this.paymentConsentService = paymentConsentService;
    }

    @Override
    protected FRFilePaymentConsent getConsent(String consentId) {
        log.debug("Retrieving file payment consent with ID {}", consentId);
        return paymentConsentService.getConsent(consentId, FRFilePaymentConsent.class);
    }

    @Override
    protected String getDebitAccountId(FRFilePaymentConsent paymentConsent) {
        OBWriteFile2DataInitiation initiation = paymentConsent.getData().getInitiation();
        if (initiation.getDebtorAccount() == null) {
            return null;
        }
        return initiation.getDebtorAccount().getIdentification();
    }

    @Override
    protected ConsentDetails buildResponse(FRFilePaymentConsent paymentConsent,
                                           List<FRAccountWithBalance> accounts,
                                           Tpp tpp) {
        OBWriteFile2DataInitiation initiation = paymentConsent.getData().getInitiation();
        return FilePaymentConsentDetails.builder()
                .accounts(accounts)
                .username(paymentConsent.getResourceOwnerUsername())
                .clientId(tpp.getClientId())
                .merchantName(paymentConsent.getOauth2ClientName())
                .fileReference(initiation.getFileReference())
                .numberOfTransactions(initiation.getNumberOfTransactions())
                .totalAmount(FRAmount.builder()
                        .amount(initiation.getControlSum().toPlainString())
                        // TODO - will this always be GBP?
                        .currency("GBP")
                        .build())
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .paymentReference(Optional.ofNullable(
                        initiation.getRemittanceInformation())
                        .map(OBWriteDomestic2DataInitiationRemittanceInformation::getReference)
                        .orElse(""))
                .build();
    }
}
