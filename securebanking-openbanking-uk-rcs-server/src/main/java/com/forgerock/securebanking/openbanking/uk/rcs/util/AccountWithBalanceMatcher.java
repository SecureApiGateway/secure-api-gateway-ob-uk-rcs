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
package com.forgerock.securebanking.openbanking.uk.rcs.util;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRAccountIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.isEmpty;

@Slf4j
public class AccountWithBalanceMatcher {

    public static Optional<FRAccountWithBalance> getMatchingAccount(String identification,
                                                                        List<FRAccountWithBalance> accounts) {
        if (isEmpty(identification)) {
            log.error("Debtor account has null or empty identification string");
            return Optional.empty();
        }
        for (FRAccountWithBalance account : accounts) {
            if (!CollectionUtils.isEmpty(account.getAccount().getAccounts())) {
                for (FRAccountIdentifier accountIdentifier : account.getAccount().getAccounts()) {
                    if (identification.equals(accountIdentifier.getIdentification())) {
                        log.debug("Found matching user account to provided debtor account. Identification: {}. " +
                                "Account Id: {}", accountIdentifier.getIdentification(), account.getId());
                        return Optional.of(account);
                    }
                }
            }
        }
        log.debug("A user account matching the identification: {} was not found", identification);
        return Optional.empty();
    }
}