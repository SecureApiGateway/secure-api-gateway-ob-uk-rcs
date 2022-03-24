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
package com.forgerock.securebanking.platform.client.test.support;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.securebanking.platform.client.ConsentStatusCode;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentDataDetails;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentDetails;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Test data factory for {@link AccountConsentDetails}
 */
public class AccountAccessConsentDetailsTestFactory {

    public static final Gson gson = new Gson();

    public static JsonObject aValidAccountConsentDetails() {
        AccountConsentDetails consentDetails = aValidAccountConsentDetailsBuilder(randomUUID().toString()).build();
        return new JsonParser().parse(gson.toJson(consentDetails)).getAsJsonObject();
    }

    public static JsonObject aValidAccountConsentDetails(String consentId) {
        AccountConsentDetails consentDetails = aValidAccountConsentDetailsBuilder(consentId).build();
        return new JsonParser().parse(gson.toJson(consentDetails)).getAsJsonObject();
    }

    public static AccountConsentDetails.AccountConsentDetailsBuilder aValidAccountConsentDetailsBuilder(String consentId) {
        return AccountConsentDetails.builder()
                .id(UUID.randomUUID().toString())
                .data(aValidAccountConsentDataDetailsBuilder(consentId).build())
                .resourceOwnerUsername(null)
                .oauth2ClientId(randomUUID().toString())
                .oauth2ClientName("AISP Name")
                .accountIds(List.of(UUID.randomUUID().toString()));
    }

    public static AccountConsentDataDetails.AccountConsentDataDetailsBuilder aValidAccountConsentDataDetailsBuilder(String consentId) {
        return AccountConsentDataDetails.builder()
                .consentId(consentId)
                .permissions(List.of(
                                FRExternalPermissionsCode.READACCOUNTSDETAIL,
                                FRExternalPermissionsCode.READBALANCES,
                                FRExternalPermissionsCode.READTRANSACTIONSDETAIL
                        )
                )
                .expirationDateTime(DateTime.now().plusDays(1))
                .creationDateTime(DateTime.now())
                .statusUpdateDateTime(DateTime.now())
                .transactionFromDateTime(DateTime.now().minusDays(1))
                .transactionToDateTime(DateTime.now())
                .status(ConsentStatusCode.AWAITINGAUTHORISATION.toString());
    }
}
