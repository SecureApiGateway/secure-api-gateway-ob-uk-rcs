/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
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

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.*;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.*;
import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.ApiClient;
import com.forgerock.securebanking.platform.client.models.ConsentRequest;
import com.google.gson.JsonObject;

import static java.util.Objects.requireNonNull;

/**
 * Factory to build the rcs {@link ConsentDetails} object from Platform Consent
 */
public class ConsentDetailsBuilderFactory {

    public static final ConsentDetails build(
            JsonObject consent,
            ConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) throws ExceptionClient {
        requireNonNull(consent, "(ConsentDetailsConverterFactory#build) parameter 'consent' cannot be null");
        requireNonNull(consentDetailsRequest, "(ConsentDetailsConverterFactory#build) parameter 'consentDetailsRequest' cannot be null");
        requireNonNull(apiClient, "(ConsentDetailsConverterFactory#build) parameter 'apiClient' cannot be null");
        String intentId = consentDetailsRequest.getIntentId();
        IntentType intentType = IntentType.identify(intentId);
        switch (intentType) {
            case ACCOUNT_ACCESS_CONSENT -> {
                return buildAccountConsentDetails(consent, consentDetailsRequest, apiClient);
            }
            case PAYMENT_DOMESTIC_CONSENT -> {
                return buildDomesticPaymentConsentDetails(consent, consentDetailsRequest, apiClient);
            }
            case PAYMENT_DOMESTIC_SCHEDULED_CONSENT -> {
                return buildDomesticScheduledPaymentConsentDetails(consent, consentDetailsRequest, apiClient);
            }
            case PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT -> {
                return buildDomesticStandingOrderConsentDetails(consent, consentDetailsRequest, apiClient);
            }
            case PAYMENT_INTERNATIONAL_CONSENT -> {
                return buildInternationalPaymentConsentDetails(consent, consentDetailsRequest, apiClient);
            }
            case PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT -> {
                return buildInternationalScheduledPaymentConsentDetails(consent, consentDetailsRequest, apiClient);
            }
            case PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT -> {
                return buildInternationalStandingOrderConsentDetails(consent, consentDetailsRequest, apiClient);
            }
            default -> {
                String message = String.format("Invalid type for intent ID: '%s'", intentId);
                throw new ExceptionClient(consentDetailsRequest, ErrorType.UNKNOWN_INTENT_TYPE, message);
            }
        }
    }

    private static final AccountsConsentDetails buildAccountConsentDetails(
            JsonObject consentDetails,
            ConsentRequest consentDetailsRequest,
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

    private static final DomesticPaymentConsentDetails buildDomesticPaymentConsentDetails(
            JsonObject consentDetails,
            ConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) {
        DomesticPaymentConsentDetailsConverter consentDetailsConverter = DomesticPaymentConsentDetailsConverter.getInstance();
        DomesticPaymentConsentDetails details = consentDetailsConverter.toDomesticPaymentConsentDetails(consentDetails);
        details.setUsername(consentDetailsRequest.getUser().getUserName());
        details.setUserId(consentDetailsRequest.getUser().getId());
        details.setAccounts(consentDetailsRequest.getAccounts());
        details.setClientId(consentDetailsRequest.getClientId());
        details.setLogo(apiClient.getLogoUri());
        return details;
    }

    private static final DomesticScheduledPaymentConsentDetails buildDomesticScheduledPaymentConsentDetails(
            JsonObject consentDetails,
            ConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) {
        DomesticScheduledPaymentConsentDetailsConverter consentDetailsConverter = DomesticScheduledPaymentConsentDetailsConverter.getInstance();
        DomesticScheduledPaymentConsentDetails details = consentDetailsConverter.toDomesticScheduledPaymentConsentDetails(consentDetails);
        details.setUsername(consentDetailsRequest.getUser().getUserName());
        details.setUserId(consentDetailsRequest.getUser().getId());
        details.setAccounts(consentDetailsRequest.getAccounts());
        details.setClientId(consentDetailsRequest.getClientId());
        details.setLogo(apiClient.getLogoUri());
        return details;
    }

    private static final DomesticStandingOrderConsentDetails buildDomesticStandingOrderConsentDetails(
            JsonObject consentDetails,
            ConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) {
        DomesticStandingOrderConsentDetailsConverter consentDetailsConverter = DomesticStandingOrderConsentDetailsConverter.getInstance();
        DomesticStandingOrderConsentDetails details = consentDetailsConverter.toDomesticStandingOrderConsentDetails(consentDetails);
        details.setUsername(consentDetailsRequest.getUser().getUserName());
        details.setUserId(consentDetailsRequest.getUser().getId());
        details.setAccounts(consentDetailsRequest.getAccounts());
        details.setClientId(consentDetailsRequest.getClientId());
        details.setLogo(apiClient.getLogoUri());
        return details;
    }

    private static final InternationalPaymentConsentDetails buildInternationalPaymentConsentDetails(
            JsonObject consentDetails,
            ConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) {
        InternationalPaymentConsentDetailsConverter consentDetailsConverter = InternationalPaymentConsentDetailsConverter.getInstance();
        InternationalPaymentConsentDetails details = consentDetailsConverter.toInternationalPaymentConsentDetails(consentDetails);
        details.setUsername(consentDetailsRequest.getUser().getUserName());
        details.setUserId(consentDetailsRequest.getUser().getId());
        details.setAccounts(consentDetailsRequest.getAccounts());
        details.setClientId(consentDetailsRequest.getClientId());
        details.setLogo(apiClient.getLogoUri());
        return details;
    }

    private static final InternationalScheduledPaymentConsentDetails buildInternationalScheduledPaymentConsentDetails(
            JsonObject consentDetails,
            ConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) {
        InternationalScheduledPaymentConsentDetailsConverter consentDetailsConverter = InternationalScheduledPaymentConsentDetailsConverter.getInstance();
        InternationalScheduledPaymentConsentDetails details = consentDetailsConverter.toInternationalScheduledPaymentConsentDetails(consentDetails);
        details.setUsername(consentDetailsRequest.getUser().getUserName());
        details.setUserId(consentDetailsRequest.getUser().getId());
        details.setAccounts(consentDetailsRequest.getAccounts());
        details.setClientId(consentDetailsRequest.getClientId());
        details.setLogo(apiClient.getLogoUri());
        return details;
    }

    private static final InternationalStandingOrderConsentDetails buildInternationalStandingOrderConsentDetails(
            JsonObject consentDetails,
            ConsentRequest consentDetailsRequest,
            ApiClient apiClient
    ) {
        InternationalStandingOrderConsentDetailsConverter consentDetailsConverter = InternationalStandingOrderConsentDetailsConverter.getInstance();
        InternationalStandingOrderConsentDetails details = consentDetailsConverter.toInternationalStandingOrderConsentDetails(consentDetails);
        details.setUsername(consentDetailsRequest.getUser().getUserName());
        details.setUserId(consentDetailsRequest.getUser().getId());
        details.setAccounts(consentDetailsRequest.getAccounts());
        details.setClientId(consentDetailsRequest.getClientId());
        details.setLogo(apiClient.getLogoUri());
        return details;
    }

}
