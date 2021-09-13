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
package com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRAccountAccessConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class FRAccountAccessConsentTestDataFactory {
    public FRAccountAccessConsentTestDataFactory() {
    }

    public static FRAccountAccessConsent aValidFRAccountAccessConsent() {
        return aValidFRAccountAccessConsentBuilder().build();
    }

    public static FRAccountAccessConsent.FRAccountAccessConsentBuilder aValidFRAccountAccessConsentBuilder() {
        return FRAccountAccessConsent.builder().id(UUID.randomUUID().toString()).clientId(UUID.randomUUID().toString()).userId(UUID.randomUUID().toString()).aispId(UUID.randomUUID().toString()).aispName("AISP Name").status(FRConsentStatusCode.AWAITINGAUTHORISATION).accountIds(List.of(UUID.randomUUID().toString())).permissions(List.of(FRExternalPermissionsCode.READACCOUNTSDETAIL, FRExternalPermissionsCode.READBENEFICIARIESDETAIL, FRExternalPermissionsCode.READSCHEDULEDPAYMENTSDETAIL, FRExternalPermissionsCode.READSTANDINGORDERSDETAIL, FRExternalPermissionsCode.READSTATEMENTSDETAIL, FRExternalPermissionsCode.READTRANSACTIONSDETAIL)).expirationDateTime(DateTime.now().plusDays(1)).transactionFromDateTime(DateTime.now().minusDays(1)).transactionToDateTime(DateTime.now());
    }
}
