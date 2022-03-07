/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRCashBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAccountIdentifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.FRAccountIdentifierTestDataFactory.aValidFRAccountIdentifier2;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.FRAccountIdentifierTestDataFactory.aValidFRAccountIdentifierBuilder;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRCashBalanceTestDataFactory.aValidFRCashBalance;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccountBuilder;
import static com.forgerock.securebanking.openbanking.uk.rcs.util.AccountWithBalanceMatcher.getMatchingAccount;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link AccountWithBalanceMatcher}.
 */
public class AccountWithBalanceMatcherTest {

    @Test
    public void shouldGetMatchingAccount() {
        // Given
        String identification1 = "40400412345678";
        String identification2 = "404004887654321";
        List<FRAccountWithBalance> accounts = List.of(
                aValidFRAccountWithBalance(identification1),
                aValidFRAccountWithBalance(identification2)
        );

        // When
        Optional<FRAccountWithBalance> matchingAccount = getMatchingAccount(identification1, accounts);

        // Then
        assertThat(matchingAccount.isPresent()).isTrue();
        List<FRAccountIdentifier> matchedAccounts = matchingAccount.get().getAccount().getAccounts();
        assertThat(matchedAccounts.get(0).getIdentification()).isEqualTo(identification1);
    }

    @Test
    public void shouldNotGetMatchingAccountGivenEmptyAccounts() {
        // Given
        String identification = "40400412345678";
        List<FRAccountWithBalance> accounts = emptyList();

        // When
        Optional<FRAccountWithBalance> matchingAccount = getMatchingAccount(identification, accounts);

        // Then
        assertThat(matchingAccount.isPresent()).isFalse();
    }

    @Test
    public void shouldNotGetMatchingAccountGivenAccountNotInList() {
        // Given
        String identification = "40400412345678";
        List<FRAccountWithBalance> accounts = List.of(
                aValidFRAccountWithBalance("40121011223344")
        );

        // When
        Optional<FRAccountWithBalance> matchingAccount = getMatchingAccount(identification, accounts);

        // Then
        assertThat(matchingAccount.isPresent()).isFalse();
    }

    private FRAccountWithBalance aValidFRAccountWithBalance(String identification) {
        FRFinancialAccount financialAccount = aValidFRFinancialAccountBuilder()
                .accounts(List.of(
                        aValidFRAccountIdentifierBuilder().identification(identification).build(),
                        aValidFRAccountIdentifier2()))
                .build();
        FRAccount bankAccount = FRAccount.builder()
                .id(UUID.randomUUID().toString())
                .account(financialAccount)
                .build();
        List<FRCashBalance> balances = List.of(aValidFRCashBalance());
        return new FRAccountWithBalance(bankAccount, balances);
    }
}
