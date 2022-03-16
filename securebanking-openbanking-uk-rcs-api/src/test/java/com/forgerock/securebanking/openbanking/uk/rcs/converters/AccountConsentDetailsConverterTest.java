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
package com.forgerock.securebanking.openbanking.uk.rcs.converters;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.accounts.AccountConsentDetailsConverter;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentDetails;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link AccountConsentDetailsConverter}
 */
@Slf4j
public class AccountConsentDetailsConverterTest {

    @Test
    public void shouldConvertAccountConsentDetailsToAccountsConsentDetails() {
        // Given
        AccountConsentDetails AccountConsentDetails = aValidAccountConsentDetails("AAC_asdfasdfasdf");
        // When
        AccountsConsentDetails accountsConsentDetails = AccountConsentDetailsConverter.getInstance().toAccountConsentDetails(AccountConsentDetails);
        // Then
        assertThat(accountsConsentDetails.getPermissions()).isEqualTo(AccountConsentDetails.getData().getPermissions());
        assertThat(accountsConsentDetails.getFromTransaction()).isEqualTo(AccountConsentDetails.getData().getTransactionFromDateTime());
        assertThat(accountsConsentDetails.getToTransaction()).isEqualTo(AccountConsentDetails.getData().getTransactionToDateTime());
        assertThat(accountsConsentDetails.getAispName()).isEqualTo(AccountConsentDetails.getOauth2ClientName());
        assertThat(accountsConsentDetails.getExpiredDate()).isEqualTo(AccountConsentDetails.getData().getExpirationDateTime());
    }
}
