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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.PaymentsConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.exception.InvalidConsentException;

@Component
public class DebtorAccountService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AccountService accountService;

    public DebtorAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * DebtorAccount is optional, but the PISP could provide the account identification details for the PSU.
     * If the account identifier match with an existing one for that psu then we update the debtor account with the proper accountId
     * and set the debtor account with balance as accounts to be display in the consent UI
     */
    public void setDebtorAccountWithBalance(PaymentsConsentDetails details, String consentRequestJws, String intentId) {
        FRAccountIdentifier debtorAccount = details.getDebtorAccount();
        if (Objects.nonNull(debtorAccount)) {
            FRAccountWithBalance accountWithBalance = accountService.getAccountWithBalanceByIdentifiers(
                    details.getUserId(), debtorAccount.getName(), debtorAccount.getIdentification(), debtorAccount.getSchemeName()
            );
            if (Objects.nonNull(accountWithBalance)) {
                debtorAccount.setAccountId(accountWithBalance.getAccount().getAccountId());
                details.setAccounts(List.of(accountWithBalance));
            } else {
                String message = String.format("Invalid debtor account provide in the consent for the intent ID: '%s', the debtor account provided in the consent doesn't exist", intentId);
                logger.error(message);
                throw new InvalidConsentException(consentRequestJws, ErrorType.ACCOUNT_SELECTION_REQUIRED,
                        OBRIErrorType.REQUEST_BINDING_FAILED, message,
                        details.getClientId(),
                        intentId);
            }
        }
    }
}
