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
package com.forgerock.securebanking.openbanking.uk.rcs.converters.general;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticPaymentsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.accounts.AccountConsentDetailsConverter;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.domestic.payments.DomesticPaymentConsentDetailsConverter;
import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentDetails;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentRequest;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentDetails;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentRequest;
import com.forgerock.securebanking.platform.client.models.general.ApiClient;
import com.forgerock.securebanking.platform.client.models.general.Consent;
import com.forgerock.securebanking.platform.client.models.general.ConsentRequest;

import static java.util.Objects.requireNonNull;

/**
 * Factory to build the rcs {@link ConsentDetails} object from Platform {@link Consent} object
 */
public class ConsentDetailsBuilderFactory {

    public static final ConsentDetails build(
            Consent consent,
            ConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) {
        requireNonNull(consent, "(ConsentDetailsConverterFactory#build) parameter 'consent' cannot be null");
        requireNonNull(consentDetailsRequest, "(ConsentDetailsConverterFactory#build) parameter 'consentDetailsRequest' cannot be null");
        requireNonNull(apiClient, "(ConsentDetailsConverterFactory#build) parameter 'apiClient' cannot be null");
        IntentType intentType = consent.getIntentType();
        switch (intentType) {
            case ACCOUNT_ACCESS_CONSENT -> {
                return buildAccountConsentDetails((AccountConsentDetails) consent, (AccountConsentRequest) consentDetailsRequest, apiClient);
            }
            case PAYMENT_DOMESTIC_CONSENT -> {
                return buildDomesticPaymentConsentDetails((DomesticPaymentConsentDetails) consent, (DomesticPaymentConsentRequest) consentDetailsRequest, apiClient);
            }
        }
        return null;
    }

    private static final AccountsConsentDetails buildAccountConsentDetails(
            AccountConsentDetails consentDetails,
            AccountConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) {
        AccountConsentDetailsConverter consentDetailsConverter = AccountConsentDetailsConverter.getInstance();
        AccountsConsentDetails details = consentDetailsConverter.toAccountConsentDetails(consentDetails);
        details.setUsername(consentDetailsRequest.getUser().getUserName());
        details.setUserId(consentDetailsRequest.getUser().getId());
        details.setAccounts(consentDetailsRequest.getAccounts());
        details.setClientId(consentDetailsRequest.getClientId());
        details.setLogo(apiClient.getLogoUri());
        return details;
    }

    private static final DomesticPaymentsConsentDetails buildDomesticPaymentConsentDetails(
            DomesticPaymentConsentDetails consentDetails,
            DomesticPaymentConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) {
        DomesticPaymentConsentDetailsConverter consentDetailsConverter = DomesticPaymentConsentDetailsConverter.getInstance();
        DomesticPaymentsConsentDetails details = consentDetailsConverter.toDomesticPaymentConsentDetails(consentDetails);
        details.setUsername(consentDetailsRequest.getUser().getUserName());
        details.setUserId(consentDetailsRequest.getUser().getId());
        details.setAccounts(consentDetailsRequest.getAccounts());
        details.setClientId(consentDetailsRequest.getClientId());
        details.setLogo(apiClient.getLogoUri());
        return details;
    }
}
