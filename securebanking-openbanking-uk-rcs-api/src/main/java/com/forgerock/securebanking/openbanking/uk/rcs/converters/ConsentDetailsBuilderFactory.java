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
package com.forgerock.securebanking.openbanking.uk.rcs.converters;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.models.AccountConsentDetails;
import com.forgerock.securebanking.platform.client.models.ApiClient;
import com.forgerock.securebanking.platform.client.models.Consent;
import com.forgerock.securebanking.platform.client.models.ConsentRequest;

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
                return buildAccountConsentDetails((AccountConsentDetails) consent, consentDetailsRequest, apiClient);
            }
        }
        return null;
    }

    private static final AccountsConsentDetails buildAccountConsentDetails(
            AccountConsentDetails accountConsentDetails,
            ConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) {
        AccountConsentDetailsConverter accountConsentDetailsConverter = AccountConsentDetailsConverter.getInstance();
        AccountsConsentDetails accountsConsentDetails = accountConsentDetailsConverter.toAccountConsentDetails(accountConsentDetails);
        accountsConsentDetails.setUsername(consentDetailsRequest.getUser().getUserName());
        accountsConsentDetails.setUserId(consentDetailsRequest.getUser().getId());
        accountsConsentDetails.setAccounts(consentDetailsRequest.getAccounts());
        accountsConsentDetails.setClientId(consentDetailsRequest.getClientId());
        accountsConsentDetails.setLogo(apiClient.getLogoUri());
        return accountsConsentDetails;
    }
}
