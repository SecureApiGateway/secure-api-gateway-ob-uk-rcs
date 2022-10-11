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
package com.forgerock.securebanking.openbanking.uk.rcs.converters;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

import org.junit.jupiter.api.Test;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.ACCOUNT_INTENT_ID;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.transformationForPermissionsList;
import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDataDetailsBuilderOnlyMandatoryFields;
import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link AccountConsentDetailsConverter}
 */
@Slf4j
public class AccountConsentDetailsConverterTest {

    @Test
    public void shouldConvertConsentDetailsAllFieldsToAccountsConsentDetails() {
        // Given
        JsonObject consentDetails = aValidAccountConsentDetails(ACCOUNT_INTENT_ID);

        // When
        AccountsConsentDetails accountsConsentDetails = AccountConsentDetailsConverter.getInstance().toAccountConsentDetails(consentDetails);

        // Then
        final JsonObject consentDetailsData = consentDetails.getAsJsonObject("OBIntentObject").getAsJsonObject("Data");
        assertThat(transformationForPermissionsList(accountsConsentDetails.getPermissions()))
                .isEqualTo(consentDetailsData.getAsJsonArray("Permissions"));

        assertThat(accountsConsentDetails.getFromTransaction().toString())
                .isEqualTo(consentDetailsData.get("TransactionFromDateTime").getAsString());

        assertThat(accountsConsentDetails.getToTransaction().toString())
                .isEqualTo(consentDetailsData.get("TransactionToDateTime").getAsString());

        assertThat(accountsConsentDetails.getAispName()).isEqualTo(consentDetails.get("oauth2ClientName").getAsString());

        assertThat(accountsConsentDetails.getExpiredDate().toString())
                .isEqualTo(consentDetailsData.get("ExpirationDateTime").getAsString());
    }

    @Test
    public void shouldConvertConsentDetailsOnlyMandatoryFieldsToAccountsConsentDetails() {
        // Given
        JsonObject consentDetails = aValidAccountConsentDetails(ACCOUNT_INTENT_ID);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data", aValidAccountConsentDataDetailsBuilderOnlyMandatoryFields(ACCOUNT_INTENT_ID));
        consentDetails.add("OBIntentObject", obIntentObject);

        // When
        AccountsConsentDetails accountsConsentDetails = AccountConsentDetailsConverter.getInstance().toAccountConsentDetails(consentDetails);

        // Then
        assertThat(transformationForPermissionsList(accountsConsentDetails.getPermissions()))
                .isEqualTo(consentDetails.getAsJsonObject("OBIntentObject").getAsJsonObject("Data").getAsJsonArray("Permissions"));

        assertThat(accountsConsentDetails.getFromTransaction()).isNull();
        assertThat(accountsConsentDetails.getToTransaction()).isNull();
        assertThat(accountsConsentDetails.getAispName()).isEqualTo(consentDetails.get("oauth2ClientName").getAsString());
        assertThat(accountsConsentDetails.getExpiredDate()).isNull();
    }


}
