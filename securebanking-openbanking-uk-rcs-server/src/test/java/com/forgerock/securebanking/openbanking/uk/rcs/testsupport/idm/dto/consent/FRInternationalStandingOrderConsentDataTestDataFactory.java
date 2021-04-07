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

import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRInternationalStandingOrderConsentData;
import org.joda.time.DateTime;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsentResponse7Data;

import static com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode.AWAITINGAUTHORISATION;
import static java.util.UUID.randomUUID;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalStandingOrderConsentTestDataFactory.aValidOBWriteInternationalStandingOrder4DataInitiation;

public class FRInternationalStandingOrderConsentDataTestDataFactory {

    public static FRInternationalStandingOrderConsentData aValidInternationalStandingOrderConsentData() {
        return aValidInternationalStandingOrderConsentDataBuilder(randomUUID().toString()).build();
    }

    public static FRInternationalStandingOrderConsentData aValidInternationalStandingOrderConsentData(String consentId) {
        return aValidInternationalStandingOrderConsentDataBuilder(consentId).build();
    }

    public static FRInternationalStandingOrderConsentData.FRInternationalStandingOrderConsentDataBuilder aValidInternationalStandingOrderConsentDataBuilder(String consentId) {
        return FRInternationalStandingOrderConsentData.builder()
               .consentId(consentId)
                .creationDateTime(DateTime.now())
                .status(AWAITINGAUTHORISATION)
                .permission(OBWriteInternationalStandingOrderConsentResponse7Data.PermissionEnum.CREATE)
                .statusUpdateDateTime(DateTime.now())
                .initiation(aValidOBWriteInternationalStandingOrder4DataInitiation());
    }
}
