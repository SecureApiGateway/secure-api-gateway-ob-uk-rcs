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
package com.forgerock.securebanking.openbanking.uk.rcs.service.decision;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccount;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRPaymentConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.client.rs.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Consumer;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_DECISION_INVALID_ACCOUNT;
import static com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode.AUTHORISED;
import static com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode.REJECTED;

/**
 * Holds common business logic for the payment consent decision updates so that they are not duplicated across many delegates
 */
@Slf4j
@Component
public class PaymentConsentDecisionUpdater {

    private final AccountService accountService;

    public PaymentConsentDecisionUpdater(AccountService accountService) {
        this.accountService = accountService;
    }

    public <T extends FRPaymentConsent> void applyUpdate(String userId,
                                                         String accountId,
                                                         boolean decision,
                                                         Consumer<T> paymentConsentUpdater,
                                                         T paymentConsent) throws OBErrorException {
        if (decision) {
            if (StringUtils.isEmpty(accountId)) {
                log.error("No account was selected for payment by user {} for consent: {}", userId, paymentConsent);
                throw new IllegalArgumentException("Missing account id");
            }
            List<FRAccount> accounts = accountService.getAccounts(userId);
            boolean userAccount = accounts.stream().anyMatch(account -> account.getId().equals(accountId));
            if (!userAccount) {
                log.error("The account selected [{}] is not owned by this user {}. List accounts {}", accountId, userId, accounts);
                throw new OBErrorException(RCS_CONSENT_DECISION_INVALID_ACCOUNT, userId, accountId, accounts);
            }

            paymentConsent.getData().setStatus(AUTHORISED);
            paymentConsent.setAccountId(accountId);
        } else {
            log.debug("The current payment consent: '{}' has been rejected by the PSU: {}", paymentConsent.getId(), userId);
            paymentConsent.getData().setStatus(REJECTED);
        }
        paymentConsentUpdater.accept(paymentConsent);
    }
}
