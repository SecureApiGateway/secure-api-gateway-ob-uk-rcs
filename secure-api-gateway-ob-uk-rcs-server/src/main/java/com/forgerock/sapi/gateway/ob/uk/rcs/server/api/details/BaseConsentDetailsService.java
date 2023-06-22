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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.BaseConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

public abstract class BaseConsentDetailsService<T extends BaseConsentEntity, D extends ConsentDetails> implements ConsentStoreDetailsService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IntentType supportedIntentType;

    protected final ConsentService<T, ?> consentService;
    private final ApiProviderConfiguration apiProviderConfiguration;
    private final ApiClientServiceClient apiClientService;

    public BaseConsentDetailsService(IntentType supportedIntentType, ConsentService<T, ?> consentService, ApiProviderConfiguration apiProviderConfiguration, ApiClientServiceClient apiClientService) {
        this.supportedIntentType = Objects.requireNonNull(supportedIntentType, "supportedIntentType must be provided");
        this.consentService = Objects.requireNonNull(consentService, "consentService must be provided");
        this.apiProviderConfiguration = Objects.requireNonNull(apiProviderConfiguration, "apiProviderConfiguration must be provided");
        this.apiClientService = Objects.requireNonNull(apiClientService, "apiClientService must be provided");
    }

    @Override
    public ConsentDetails getDetailsFromConsentStore(ConsentClientDetailsRequest consentClientRequest) throws ExceptionClient {
        final T consent = getConsent(consentClientRequest);

        final D consentDetails = createConsentDetailsObject();
        populateCommonConsentDetailsFields(consentDetails, consentClientRequest);
        addIntentTypeSpecificData(consentDetails, consent, consentClientRequest);

        return consentDetails;
    }

    /**
     * @return any initialized ConsentDetails object
     */
    protected abstract D createConsentDetailsObject();

    protected abstract void addIntentTypeSpecificData(D consentDetails, T consent, ConsentClientDetailsRequest consentClientDetailsRequest);

    private T getConsent(ConsentClientDetailsRequest consentClientRequest) {
        final String clientId = consentClientRequest.getClientId();
        final String intentId = consentClientRequest.getIntentId();
        logger.info("Fetching Data from RCS Consent Service - consentId: {}, clientId: {}", intentId, clientId);
        final T consent = consentService.getConsent(intentId, clientId);
        logger.info("Got consent: {}", consent);
        return consent;
    }

    private void populateCommonConsentDetailsFields(ConsentDetails details, ConsentClientDetailsRequest consentClientRequest) throws ExceptionClient {
        details.setConsentId(consentClientRequest.getIntentId());
        details.setUsername(consentClientRequest.getUser().getUserName());
        details.setUserId(consentClientRequest.getUser().getId());
        details.setClientId(consentClientRequest.getClientId());
        details.setServiceProviderName(apiProviderConfiguration.getName());

        final ApiClient apiClient = apiClientService.getApiClient(consentClientRequest.getClientId());
        details.setLogo(apiClient.getLogoUri());
        details.setClientName(apiClient.getName());
    }

    @Override
    public IntentType getSupportedIntentType() {
        return supportedIntentType;
    }

}
