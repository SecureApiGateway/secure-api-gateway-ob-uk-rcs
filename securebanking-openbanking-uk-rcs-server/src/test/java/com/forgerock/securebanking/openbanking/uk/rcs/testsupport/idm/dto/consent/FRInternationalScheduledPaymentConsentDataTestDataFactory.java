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

import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRInternationalScheduledPaymentConsentData;
import org.joda.time.DateTime;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsentResponse6Data;

import static com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode.AWAITINGAUTHORISATION;
import static java.util.UUID.randomUUID;
import static uk.org.openbanking.testsupport.payment.OBExchangeRateTestDataFactory.aValidOBWriteInternationalConsentResponse6DataExchangeRateInformation;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalScheduledConsentTestDataFactory.aValidOBWriteInternationalScheduled3DataInitiation;

public class FRInternationalScheduledPaymentConsentDataTestDataFactory {

    public static FRInternationalScheduledPaymentConsentData aValidInternationalScheduledPaymentConsentData() {
        return aValidInternationalScheduledPaymentConsentDataBuilder(randomUUID().toString()).build();
    }

    public static FRInternationalScheduledPaymentConsentData aValidInternationalScheduledPaymentConsentData(String consentId) {
        return aValidInternationalScheduledPaymentConsentDataBuilder(consentId).build();
    }

    public static FRInternationalScheduledPaymentConsentData.FRInternationalScheduledPaymentConsentDataBuilder aValidInternationalScheduledPaymentConsentDataBuilder(String consentId) {
        return FRInternationalScheduledPaymentConsentData.builder()
                .consentId(consentId)
                .creationDateTime(DateTime.now())
                .status(AWAITINGAUTHORISATION)
                .permission(OBWriteInternationalScheduledConsentResponse6Data.PermissionEnum.CREATE)
                .statusUpdateDateTime(DateTime.now())
                .expectedExecutionDateTime(DateTime.now())
                .expectedSettlementDateTime(DateTime.now().plusDays(1))
                .exchangeRateInformation(aValidOBWriteInternationalConsentResponse6DataExchangeRateInformation())
                .initiation(aValidOBWriteInternationalScheduled3DataInitiation());
    }
}
