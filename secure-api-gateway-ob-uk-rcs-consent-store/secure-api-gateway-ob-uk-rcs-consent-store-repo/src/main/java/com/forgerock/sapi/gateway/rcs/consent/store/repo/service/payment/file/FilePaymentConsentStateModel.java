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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.file;

import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentStateModel;

import uk.org.openbanking.datamodel.v3.payment.OBWriteFileConsentResponse4DataStatus;

/**
 * State Model for File Payment Consents: https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/pisp/file-payment-consents.html#state-model
 */
public class FilePaymentConsentStateModel implements ConsentStateModel {

    public static final String AWAITING_UPLOAD = OBWriteFileConsentResponse4DataStatus.AWAITINGUPLOAD.toString();
    public static final String AWAITING_AUTHORISATION = OBWriteFileConsentResponse4DataStatus.AWAITINGAUTHORISATION.toString();
    public static final String AUTHORISED = OBWriteFileConsentResponse4DataStatus.AUTHORISED.toString();
    public static final String CONSUMED = OBWriteFileConsentResponse4DataStatus.CONSUMED.toString();
    public static final String REJECTED = OBWriteFileConsentResponse4DataStatus.REJECTED.toString();

    private static final FilePaymentConsentStateModel INSTANCE = new FilePaymentConsentStateModel();

    public static FilePaymentConsentStateModel getInstance() {
        return INSTANCE;
    }

    private final MultiValueMap<String, String> stateTransitions;

    private FilePaymentConsentStateModel() {
        stateTransitions = new LinkedMultiValueMap<>();
        stateTransitions.addAll(AWAITING_UPLOAD, List.of(AWAITING_AUTHORISATION));
        stateTransitions.addAll(AWAITING_AUTHORISATION, List.of(AUTHORISED, REJECTED));
        stateTransitions.addAll(AUTHORISED, List.of(CONSUMED));
    }

    @Override
    public String getInitialConsentStatus() {
        return AWAITING_UPLOAD;
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
