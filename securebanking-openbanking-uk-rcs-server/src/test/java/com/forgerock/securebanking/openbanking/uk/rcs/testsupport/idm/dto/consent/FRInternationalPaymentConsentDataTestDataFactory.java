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

import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRInternationalPaymentConsentData;
import org.joda.time.DateTime;

import static com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode.AWAITINGAUTHORISATION;
import static java.util.UUID.randomUUID;
import static uk.org.openbanking.testsupport.payment.OBExchangeRateTestDataFactory.aValidOBWriteInternationalConsentResponse6DataExchangeRateInformation;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternational3DataInitiation;

public class FRInternationalPaymentConsentDataTestDataFactory {

    public static FRInternationalPaymentConsentData aValidInternationalPaymentConsentData() {
        return aValidInternationalPaymentConsentDataBuilder(randomUUID().toString()).build();
    }

    public static FRInternationalPaymentConsentData aValidInternationalPaymentConsentData(String consentId) {
        return aValidInternationalPaymentConsentDataBuilder(consentId).build();
    }

    public static FRInternationalPaymentConsentData.FRInternationalPaymentConsentDataBuilder aValidInternationalPaymentConsentDataBuilder(String consentId) {
        return FRInternationalPaymentConsentData.builder()
                .consentId(consentId)
                .creationDateTime(DateTime.now())
                .status(AWAITINGAUTHORISATION)
                .statusUpdateDateTime(DateTime.now())
                .expectedExecutionDateTime(DateTime.now())
                .expectedSettlementDateTime(DateTime.now().plusDays(1))
                .exchangeRateInformation(aValidOBWriteInternationalConsentResponse6DataExchangeRateInformation())
                .initiation(aValidOBWriteInternational3DataInitiation());
    }
}
