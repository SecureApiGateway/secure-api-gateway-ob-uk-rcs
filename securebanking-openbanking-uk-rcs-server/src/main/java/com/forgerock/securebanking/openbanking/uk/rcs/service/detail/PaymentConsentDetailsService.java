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
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRPaymentConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.exception.InvalidConsentException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.util.AccountWithBalanceMatcher.getMatchingAccount;
import static java.util.Collections.emptyList;

/**
 * Abstract class providing common functionality for retrieving the details of a consent for each payment type.
 *
 * @param <CONSENT> The type of the {@link FRPaymentConsent} that overriding classes are dealing with.
 */
@Slf4j
public abstract class PaymentConsentDetailsService<CONSENT extends FRPaymentConsent> implements ConsentDetailsService {

    private final PaymentConsentService paymentConsentService;
    private final TppService tppService;

    public PaymentConsentDetailsService(PaymentConsentService paymentConsentService, TppService tppService) {
        this.paymentConsentService = paymentConsentService;
        this.tppService = tppService;
    }

    @Override
    public ConsentDetails getConsentDetails(ConsentDetailsRequest request) throws OBErrorException {
        String consentRequestJwt = request.getConsentRequestJwtString();
        log.debug("Received a payment consent request with JWT: '{}'", consentRequestJwt);
        String consentId = request.getIntentId();
        log.debug("=> The payment's consent id: '{}'", consentId);
        String clientId = request.getClientId();
        log.debug("=> The client id: '{}'", clientId);

        CONSENT paymentConsent = getPaymentConsent(consentId);
        if (paymentConsent == null) {
            log.error("The PISP '{}' is referencing a payment payment consent {} that doesn't exist", clientId, consentId);
            throw new OBErrorException(PAYMENT_CONSENT_NOT_FOUND, clientId, consentId);
        }
        List<FRAccountWithBalance> accounts = request.getAccounts();

        // Only show the debtor account if specified in consent
        String debtorAccountId = getDebitAccountId(paymentConsent);
        if (debtorAccountId != null) {
            Optional<FRAccountWithBalance> matchingUserAccount = getMatchingAccount(debtorAccountId, accounts);
            if (matchingUserAccount.isEmpty()) {
                log.error("The PISP '{}' created the payment request '{}' but the debtor account: {} on the payment " +
                                "consent is not one of the user's accounts: {}.", paymentConsent.getOauth2ClientId(), consentId,
                        debtorAccountId, accounts);
                throw new InvalidConsentException(consentRequestJwt, RCS_CONSENT_REQUEST_DEBTOR_ACCOUNT_NOT_FOUND,
                        clientId, consentId, accounts);
            }
            accounts = List.of(matchingUserAccount.get());
        }

        Optional<Tpp> isTpp = tppService.getTpp(paymentConsent.getOauth2ClientId());
        if (isTpp.isEmpty()) {
            log.error("The TPP '{}' (Client ID {}) that created this consent id '{}' doesn't exist anymore.",
                    paymentConsent.getOauth2ClientId(), clientId, consentId);
            throw new InvalidConsentException(consentRequestJwt, RCS_CONSENT_REQUEST_NOT_FOUND_TPP, clientId, consentId,
                    emptyList());
        }
        Tpp tpp = isTpp.get();

        // Verify the pisp is the same than the one that created this payment
        verifyTppCreatedPaymentConsent(clientId, tpp.getClientId(), consentId);

        // Associate the payment to this user
        paymentConsent.setResourceOwnerUsername(request.getUsername());
        paymentConsentService.updateConsent(paymentConsent);

        return buildResponse(paymentConsent, accounts, tpp);
    }

    /**
     * Overriding classes need to retrieve their own type of {@link FRPaymentConsent}.
     *
     * @param consentId The ID of the {@link FRPaymentConsent} in question.
     * @return The corresponding {@link FRPaymentConsent} (should never be null).
     */
    protected abstract CONSENT getPaymentConsent(String consentId);

    /**
     * Overriding classes know which underlying OB "DataInitiation" object they're dealing with, so know how to
     * retrieve the debit account's identifier (e.g. concatenated sort code and account number).
     *
     * @param paymentConsent The {@link FRPaymentConsent} containing the payment related data.
     * @return The ID of the debit account, or null if it hasn't been provided in the consent data.
     */
    protected abstract String getDebitAccountId(CONSENT paymentConsent);

    /**
     * Overriding classes need to build the specific {@link ConsentDetails} for the payment in question.
     *
     * @param paymentConsent The {@link FRPaymentConsent} containing the payment related data.
     * @param accounts The list of user's accounts.
     * @param tpp The {@link Tpp} associated with the consent.
     * @return The specific {@link ConsentDetails} for the payment in question.
     */
    protected abstract ConsentDetails buildResponse(CONSENT paymentConsent,
                                                    List<FRAccountWithBalance> accounts,
                                                    Tpp tpp);
}
