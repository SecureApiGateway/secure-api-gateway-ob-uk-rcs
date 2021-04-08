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

import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRFundsConfirmationConsentData;
import org.joda.time.DateTime;

import static com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode.AWAITINGAUTHORISATION;
import static java.util.UUID.randomUUID;
import static uk.org.openbanking.testsupport.payment.OBAccountTestDataFactory.aValidOBCashAccount3;

public class FRFundsConfirmationConsentDataTestDataFactory {

    public static FRFundsConfirmationConsentData aValidFundsConfirmationConsentData() {
        return aValidFundsConfirmationConsentDataBuilder(randomUUID().toString()).build();
    }

    public static FRFundsConfirmationConsentData aValidFundsConfirmationConsentData(String consentId) {
        return aValidFundsConfirmationConsentDataBuilder(consentId).build();
    }

    public static FRFundsConfirmationConsentData.FRFundsConfirmationConsentDataBuilder aValidFundsConfirmationConsentDataBuilder(String consentId) {
        return FRFundsConfirmationConsentData.builder()
                .consentId(consentId)
                .creationDateTime(DateTime.now())
                .status(AWAITINGAUTHORISATION)
                .statusUpdateDateTime(DateTime.now())
                .debtorAccount(aValidOBCashAccount3());
    }
}
