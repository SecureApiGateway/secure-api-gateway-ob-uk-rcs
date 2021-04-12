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
package com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @Type(value = FRDomesticPaymentConsent.class, name = "FRDomesticPaymentConsent"),
        @Type(value = FRDomesticScheduledPaymentConsent.class, name = "FRDomesticScheduledPaymentConsent"),
        @Type(value = FRDomesticStandingOrderConsent.class, name = "FRDomesticStandingOrderConsent"),
        @Type(value = FRInternationalPaymentConsent.class, name = "FRInternationalPaymentConsent"),
        @Type(value = FRInternationalScheduledPaymentConsent.class, name = "FRInternationalScheduledPaymentConsent"),
        @Type(value = FRInternationalStandingOrderConsent.class, name = "FRInternationalStandingOrderConsent"),
        @Type(value = FRFilePaymentConsent.class, name = "FRFilePaymentConsent"),
        @Type(value = FRFundsConfirmationConsent.class, name = "FRFundsConfirmationConsent")
})
public interface FRPaymentConsent {

    String getId();

    FRPaymentConsentData getData();

    String getAccountId();

    void setAccountId(String accountId);

    String getResourceOwnerUsername();

    void setResourceOwnerUsername(String resourceOwnerUsername);

    String getOauth2ClientId();

    String getOauth2ClientName();
}
