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

import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRDomesticScheduledPaymentConsent;

import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.idm.dto.consent.FRDomesticScheduledPaymentConsentDataTestDataFactory.aValidDomesticScheduledPaymentConsentData;
import static java.util.UUID.randomUUID;

/**
 * Test data factory for {@link FRDomesticScheduledPaymentConsent}.
 */
public class FRDomesticScheduledPaymentConsentTestDataFactory {

    public static FRDomesticScheduledPaymentConsent aValidFRDomesticScheduledPaymentConsent() {
        return aValidFRDomesticScheduledPaymentConsentBuilder().build();
    }

    public static FRDomesticScheduledPaymentConsent.FRDomesticScheduledPaymentConsentBuilder aValidFRDomesticScheduledPaymentConsentBuilder() {
        return aValidFRDomesticScheduledPaymentConsentBuilder(randomUUID().toString());
    }

    public static FRDomesticScheduledPaymentConsent.FRDomesticScheduledPaymentConsentBuilder aValidFRDomesticScheduledPaymentConsentBuilder(String consentId) {
        return FRDomesticScheduledPaymentConsent.builder()
                .id(consentId)
                .data(aValidDomesticScheduledPaymentConsentData(consentId))
                .accountId(randomUUID().toString())
                .resourceOwnerUsername(randomUUID().toString())
                .oauth2ClientId(randomUUID().toString())
                .oauth2ClientName("Tpp App Name");
    }
}