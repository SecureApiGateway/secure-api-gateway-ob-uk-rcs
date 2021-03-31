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
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticScheduledPaymentConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRDomesticScheduledPaymentConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.exception.InvalidConsentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationRemittanceInformation;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduled2DataInitiation;

import java.util.List;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.FRAmountConverter.toFRAmount;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.util.AccountWithBalanceMatcher.getMatchingAccount;
import static java.util.Collections.emptyList;

@Service
@Slf4j
public class DomesticScheduledPaymentConsentDetailsService implements ConsentDetailsService {

    private final PaymentConsentService paymentConsentService;
    private final TppService tppService;

    public DomesticScheduledPaymentConsentDetailsService(PaymentConsentService paymentConsentService,
                                                         TppService tppService) {
        this.paymentConsentService = paymentConsentService;
        this.tppService = tppService;
    }

    @Override
    public DomesticScheduledPaymentConsentDetails getConsentDetails(ConsentDetailsRequest request) throws OBErrorException {
        String consentRequestJwt = request.getConsentRequestJwtString();
        log.debug("Received a domestic scheduled payment consent request with JWT: '{}'", consentRequestJwt);
        String consentId = request.getIntentId();
        log.debug("=> The payment's consent id: '{}'", consentId);
        String clientId = request.getClientId();
        log.debug("=> The client id: '{}'", clientId);

        FRDomesticScheduledPaymentConsent domesticConsent = paymentConsentService.getConsent(consentId, FRDomesticScheduledPaymentConsent.class);
        if (domesticConsent == null) {
            log.error("The PISP '{}' is referencing a domestic payment consent {} that doesn't exist", clientId, consentId);
            throw new OBErrorException(PAYMENT_CONSENT_NOT_FOUND, clientId, consentId);
        }
        List<FRAccountWithBalance> accounts = request.getAccounts();

        // Only show the debtor account if specified in consent
        OBWriteDomesticScheduled2DataInitiation initiation = domesticConsent.getData().getInitiation();
        if (initiation.getDebtorAccount() != null) {
            String identification = initiation.getDebtorAccount().getIdentification();
            Optional<FRAccountWithBalance> matchingUserAccount = getMatchingAccount(identification, accounts);
            if (matchingUserAccount.isEmpty()) {
                log.error("The PISP '{}' created the payment request '{}' but the debtor account: {} on the payment " +
                                "consent is not one of the user's accounts: {}.", domesticConsent.getOauth2ClientId(), consentId,
                        initiation.getDebtorAccount(), accounts);
                throw new InvalidConsentException(consentRequestJwt, RCS_CONSENT_REQUEST_DEBTOR_ACCOUNT_NOT_FOUND,
                        clientId, consentId, accounts);
            }
            accounts = List.of(matchingUserAccount.get());
        }

        Optional<Tpp> isTpp = tppService.getTpp(domesticConsent.getOauth2ClientId());
        if (isTpp.isEmpty()) {
            log.error("The TPP '{}' (Client ID {}) that created this consent id '{}' doesn't exist anymore.",
                    domesticConsent.getOauth2ClientId(), clientId, consentId);
            throw new InvalidConsentException(consentRequestJwt, RCS_CONSENT_REQUEST_NOT_FOUND_TPP, clientId, consentId,
                    emptyList());
        }
        Tpp tpp = isTpp.get();

        // Verify the pisp is the same than the one that created this payment
        verifyTppCreatedPaymentConsent(clientId, tpp.getClientId(), consentId);

        // Associate the payment to this user
        domesticConsent.setResourceOwnerUsername(request.getUsername());
        paymentConsentService.updateConsent(domesticConsent);

        return DomesticScheduledPaymentConsentDetails.builder()
                .instructedAmount(toFRAmount(initiation.getInstructedAmount()))
                .accounts(accounts)
                .username(request.getUsername())
                .logo(tpp.getLogoUri())
                .merchantName(domesticConsent.getOauth2ClientName())
                .clientId(clientId)
                .paymentReference(Optional.ofNullable(
                        initiation.getRemittanceInformation())
                        .map(OBWriteDomestic2DataInitiationRemittanceInformation::getReference)
                        .orElse(""))
                .build();
    }
}
