/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.vrp;

import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentStateModel;

import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentResponseData.StatusEnum;

/**
 * State model for VRP Payment APIs: https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/vrp/domestic-vrp-consents.html#state-model-vrp-consents
 *
 * VRPs share a similar state model to Account Access Consents (and other long-lived Consents), they support re-authentication and
 * unlike other payments cannot be consumed (because they are recurring).
 */
public class VRPConsentStateModel implements ConsentStateModel {

    public static final String AWAITING_AUTHORISATION = StatusEnum.AWAITINGAUTHORISATION.toString();

    public static final String AUTHORISED = StatusEnum.AUTHORISED.toString();

    public static final String REJECTED = StatusEnum.REJECTED.toString();

    private static final VRPConsentStateModel INSTANCE = new VRPConsentStateModel();

    public static VRPConsentStateModel getInstance() {
        return INSTANCE;
    }

    private final MultiValueMap<String, String> stateTransitions;

    private VRPConsentStateModel() {
        stateTransitions = new LinkedMultiValueMap<>();
        stateTransitions.addAll(AWAITING_AUTHORISATION, List.of(AUTHORISED, REJECTED));
        stateTransitions.addAll(AUTHORISED, List.of(AUTHORISED, REJECTED)); // Authorised has a self link as consent Re-Authentication is supported
    }

    @Override
    public String getInitialConsentStatus() {
        return AWAITING_AUTHORISATION;
    }

    @Override
    public String getAuthorisedConsentStatus() {
        return AUTHORISED;
    }

    @Override
    public String getRejectedConsentStatus() {
        return REJECTED;
    }

    @Override
    public String getRevokedConsentStatus() {
        return REJECTED;
    }

    @Override
    public MultiValueMap<String, String> getValidStateTransitions() {
        return new LinkedMultiValueMap<>(stateTransitions);
    }

}
