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
package com.forgerock.sapi.gateway.rcs.consent.store.api.customerinfo;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.AuthoriseCustomerInfoConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.CreateCustomerInfoConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.CustomerInfoConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.customerinfo.CustomerInfoConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo.CustomerInfoAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo.CustomerInfoConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.common.OBExternalRequestStatus1Code;

public class BaseCustomerInfoConsentApiController implements CustomerInfoConsentApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CustomerInfoConsentService consentService;
    private final OBVersion obVersion;

    public BaseCustomerInfoConsentApiController(CustomerInfoConsentService consentService, OBVersion obVersion) {
        this.consentService = requireNonNull(consentService, "consentService must be provided");
        this.obVersion = requireNonNull(obVersion, "obVersion must be provided");
    }

    @Override
    public ResponseEntity<CustomerInfoConsent> createConsent(CreateCustomerInfoConsentRequest request) {
        logger.info("Attempting to createConsent: {}", request);
        final CustomerInfoConsentEntity consentEntity = new CustomerInfoConsentEntity();
        consentEntity.setRequestObj(request.getConsentRequest());
        consentEntity.setApiClientId(request.getApiClientId());
        consentEntity.setRequestVersion(obVersion);
        consentEntity.setStatus(OBExternalRequestStatus1Code.AWAITINGAUTHORISATION.toString());

        final CustomerInfoConsentEntity persistedEntity = consentService.createConsent(consentEntity);
        logger.info("Consent created with id: {}", persistedEntity.getId());
        return new ResponseEntity<>(convertEntityToDto(persistedEntity), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CustomerInfoConsent> getConsent(String consentId, String apiClientId) {
        logger.info("Attempting to getConsent - id: {}, for apiClientId: {}", consentId, apiClientId);
        return ResponseEntity.ok(convertEntityToDto(consentService.getConsent(consentId, apiClientId)));
    }

    @Override
    public ResponseEntity<CustomerInfoConsent> authoriseConsent(
            String consentId,
            AuthoriseCustomerInfoConsentRequest request
    ) {
        logger.info("Attempting to authoriseConsent - id: {}, request: {}", consentId, request);
        final CustomerInfoAuthoriseConsentArgs authoriseArgs = new CustomerInfoAuthoriseConsentArgs(
                consentId,
                request.getApiClientId(),
                request.getResourceOwnerId()
        );
        return ResponseEntity.ok(convertEntityToDto(consentService.authoriseConsent(authoriseArgs)));
    }

    @Override
    public ResponseEntity<CustomerInfoConsent> rejectConsent(String consentId, RejectConsentRequest request) {
        logger.info("Attempting to rejectConsent - id: {}, request: {}", consentId, request);
        return ResponseEntity.ok(
                convertEntityToDto(
                        consentService.rejectConsent(
                                consentId,
                                request.getApiClientId(),
                                request.getResourceOwnerId()
                        )
                )
        );
    }

    @Override
    public ResponseEntity<Void> deleteConsent(String consentId, String apiClientId) {
        logger.info("Attempting to deleteConsent - id: {}, apiClientId: {}", consentId, apiClientId);
        consentService.deleteConsent(consentId, apiClientId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private CustomerInfoConsent convertEntityToDto(CustomerInfoConsentEntity entity) {
        final CustomerInfoConsent dto = new CustomerInfoConsent();
        dto.setApiClientId(entity.getApiClientId());
        dto.setId(entity.getId());
        dto.setCreationDateTime(entity.getCreationDateTime());
        dto.setStatus(entity.getStatus());
        dto.setStatusUpdateDateTime(entity.getStatusUpdatedDateTime());
        dto.setRequestVersion(entity.getRequestVersion());
        dto.setRequestObj(entity.getRequestObj());
        dto.setResourceOwnerId(entity.getResourceOwnerId());
        return dto;
    }
}
