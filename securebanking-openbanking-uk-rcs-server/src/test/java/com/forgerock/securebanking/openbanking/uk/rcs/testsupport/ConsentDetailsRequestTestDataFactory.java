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
package com.forgerock.securebanking.openbanking.uk.rcs.testsupport;

import com.forgerock.securebanking.openbanking.uk.rcs.service.detail.ConsentDetailsRequest;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.List;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountWithBalanceTestDataFactory.aValidFRAccountWithBalance;
import static java.util.UUID.randomUUID;

/**
 * Test data factory for {@link ConsentDetailsRequest}.
 */
public class ConsentDetailsRequestTestDataFactory {

    public static ConsentDetailsRequest aValidAccountAccessConsentDetailsRequest() {
        return aValidAccountAccessConsentDetailsRequestBuilder().build();
    }

    public static ConsentDetailsRequest aValidDomesticPaymentConsentDetailsRequest() {
        return aValidDomesticPaymentConsentDetailsRequestBuilder().build();
    }

    public static ConsentDetailsRequest.ConsentDetailsRequestBuilder aValidAccountAccessConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder("AAC_");
    }

    public static ConsentDetailsRequest.ConsentDetailsRequestBuilder aValidDomesticPaymentConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder("PDC_");
    }

    public static ConsentDetailsRequest.ConsentDetailsRequestBuilder aValidDomesticScheduledPaymentConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder("PDSC_");
    }

    public static ConsentDetailsRequest.ConsentDetailsRequestBuilder aValidDomesticStandingOrderConsentDetailsRequestBuilder() {
        return getConsentDetailsRequestBuilder("PDSOC_");
    }

    private static ConsentDetailsRequest.ConsentDetailsRequestBuilder getConsentDetailsRequestBuilder(String intentType) {
        try {
            return ConsentDetailsRequest.builder()
                    .intentId(intentType + randomUUID().toString())
                    .consentRequestJwt(SignedJWT.parse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                    .accounts(List.of(aValidFRAccountWithBalance()))
                    .username(randomUUID().toString())
                    .clientId(randomUUID().toString());
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid Signed JWT value");
        }
    }
}
