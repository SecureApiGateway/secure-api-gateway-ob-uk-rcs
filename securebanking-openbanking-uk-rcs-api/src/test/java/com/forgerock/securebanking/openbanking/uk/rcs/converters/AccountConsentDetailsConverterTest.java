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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.accounts.AccountConsentDetailsConverter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.INTENT_ID;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.transformationForPermissionsList;
import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link AccountConsentDetailsConverter}
 */
@Slf4j
public class AccountConsentDetailsConverterTest {



    @Test
    public void shouldConvertConsentDetailsAllFieldsToAccountsConsentDetails() {
        // Given
        JsonObject consentDetails = aValidAccountConsentDetails(INTENT_ID);

        // When
        AccountsConsentDetails accountsConsentDetails = AccountConsentDetailsConverter.getInstance().toAccountConsentDetails(consentDetails);

        // Then
        assertThat(transformationForPermissionsList(accountsConsentDetails.getPermissions()))
                .isEqualTo(consentDetails.getAsJsonObject("data").getAsJsonArray("Permissions"));

        assertThat(accountsConsentDetails.getFromTransaction().toString())
                .isEqualTo(consentDetails.getAsJsonObject("data").get("TransactionFromDateTime").getAsString());

        assertThat(accountsConsentDetails.getToTransaction().toString())
                .isEqualTo(consentDetails.getAsJsonObject("data").get("TransactionToDateTime").getAsString());

        assertThat(accountsConsentDetails.getAispName()).isEqualTo(consentDetails.get("oauth2ClientName").getAsString());

        assertThat(accountsConsentDetails.getExpiredDate().toString())
                .isEqualTo(consentDetails.getAsJsonObject("data").get("ExpirationDateTime").getAsString());
    }

    @Test
    public void shouldConvertConsentDetailsOnlyMandatoryFieldsToAccountsConsentDetails() {
        // Given
        JsonObject consentDetails = aValidAccountConsentDetails(INTENT_ID);
        consentDetails.add("data", aValidAccountConsentDataDetailsBuilderOnlyMandatoryFields(INTENT_ID));

        // When
        AccountsConsentDetails accountsConsentDetails = AccountConsentDetailsConverter.getInstance().toAccountConsentDetails(consentDetails);

        // Then
        assertThat(transformationForPermissionsList(accountsConsentDetails.getPermissions()))
                .isEqualTo(consentDetails.getAsJsonObject("data").getAsJsonArray("Permissions"));

        assertThat(accountsConsentDetails.getFromTransaction()).isNull();
        assertThat(accountsConsentDetails.getToTransaction()).isNull();
        assertThat(accountsConsentDetails.getAispName()).isEqualTo(consentDetails.get("oauth2ClientName").getAsString());
        assertThat(accountsConsentDetails.getExpiredDate()).isNull();
    }


}
