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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domestic.v3_1_10;

import java.util.function.Supplier;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.AuthoriseDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.ConsumeDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.RejectDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;

/**
 * Implementation of DomesticPaymentConsentApi for OBIE version 3.1.10
 *
 * Note: the obVersion field is pluggable, so if there are no changes to the OBIE schema in later versions, then
 * these controllers can extend this and configure the
 */
@Controller
public class DomesticPaymentConsentApiController implements DomesticPaymentConsentApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DomesticPaymentConsentService consentService;

    private Supplier<DateTime> idempotencyKeyExpirationSupplier;

    private final OBVersion obVersion;

    @Autowired
    public DomesticPaymentConsentApiController(DomesticPaymentConsentService consentService,
                                              Supplier<DateTime> idempotencyKeyExpirationSupplier) {
        this(consentService, idempotencyKeyExpirationSupplier, OBVersion.v3_1_10);
    }

    public DomesticPaymentConsentApiController(DomesticPaymentConsentService consentService,
                                               Supplier<DateTime> idempotencyKeyExpirationSupplier,
                                               OBVersion obVersion) {
        this.consentService = consentService;
        this.idempotencyKeyExpirationSupplier = idempotencyKeyExpirationSupplier;
        this.obVersion = obVersion;
    }

    @Override
    public ResponseEntity<DomesticPaymentConsent> createConsent(CreateDomesticPaymentConsentRequest request, String apiClientId) {
        logger.info("Attempting to createConsent: {}, for apiClientId: {}", request, apiClientId);
        final DomesticPaymentConsentEntity domesticPaymentConsent = new DomesticPaymentConsentEntity();
        domesticPaymentConsent.setRequestType(request.getConsentRequest().getClass().getSimpleName());
        domesticPaymentConsent.setRequestVersion(obVersion);
        domesticPaymentConsent.setApiClientId(request.getApiClientId());
        domesticPaymentConsent.setRequestObj(request.getConsentRequest());
        domesticPaymentConsent.setStatus(StatusEnum.AWAITINGAUTHORISATION.toString());
        domesticPaymentConsent.setCharges(request.getCharges());
        domesticPaymentConsent.setIdempotencyKey(request.getIdempotencyKey());
        domesticPaymentConsent.setIdempotencyKeyExpiration(idempotencyKeyExpirationSupplier.get());
        final DomesticPaymentConsentEntity persistedEntity = consentService.createConsent(domesticPaymentConsent);
        logger.info("Consent created with id: {}", persistedEntity.getId());

        return new ResponseEntity<>(convertEntityToDto(persistedEntity), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<DomesticPaymentConsent> getConsent(String consentId, String apiClientId) {
        logger.info("Attempting to getConsent - id: {}, for apiClientId: {}", consentId, apiClientId);
        return ResponseEntity.ok(convertEntityToDto(consentService.getConsent(consentId, apiClientId)));
    }

    @Override
    public ResponseEntity<DomesticPaymentConsent> authoriseConsent(String consentId, AuthoriseDomesticPaymentConsentRequest request, String apiClientId) {
        logger.info("Attempting to authoriseConsent - id: {}, request: {}, apiClientId: {}", consentId, request, apiClientId);
        final DomesticPaymentAuthoriseConsentArgs domesticPaymentAuthoriseConsentArgs = new DomesticPaymentAuthoriseConsentArgs(consentId, apiClientId,
                                                                                                                                request.getResourceOwnerId(), request.getAuthorisedDebtorAccountId());
        return ResponseEntity.ok(convertEntityToDto(consentService.authoriseConsent(domesticPaymentAuthoriseConsentArgs)));
    }

    @Override
    public ResponseEntity<DomesticPaymentConsent> rejectConsent(String consentId, RejectDomesticPaymentConsentRequest request, String apiClientId) {
        logger.info("Attempting to rejectConsent - id: {}, request: {}, apiClientId: {}", consentId, request, apiClientId);
        return ResponseEntity.ok(convertEntityToDto(consentService.rejectConsent(consentId, apiClientId, request.getResourceOwnerId())));
    }

    @Override
    public ResponseEntity<DomesticPaymentConsent> consumeConsent(String consentId, ConsumeDomesticPaymentConsentRequest request, String apiClientId) {
        logger.info("Attempting to consumeConsent - id: {}, request: {}, apiClientId: {}", consentId, request, apiClientId);
        return ResponseEntity.ok(convertEntityToDto(consentService.consumeConsent(consentId, apiClientId)));
    }

    private DomesticPaymentConsent convertEntityToDto(DomesticPaymentConsentEntity entity) {
        final DomesticPaymentConsent dto = new DomesticPaymentConsent();
        dto.setId(entity.getId());
        dto.setStatus(entity.getStatus());
        dto.setRequestObj(entity.getRequestObj());
        dto.setRequestType(entity.getRequestType());
        dto.setRequestVersion(entity.getRequestVersion());
        dto.setApiClientId(entity.getApiClientId());
        dto.setResourceOwnerId(entity.getResourceOwnerId());
        dto.setAuthorisedDebtorAccountId(entity.getAuthorisedDebtorAccountId());
        dto.setIdempotencyKey(entity.getIdempotencyKey());
        dto.setIdempotencyKeyExpiration(entity.getIdempotencyKeyExpiration());
        dto.setCharges(entity.getCharges());
        dto.setCreationDateTime(entity.getCreationDateTime());
        dto.setStatusUpdateDateTime(entity.getStatusUpdatedDateTime());
        return dto;
    }
}