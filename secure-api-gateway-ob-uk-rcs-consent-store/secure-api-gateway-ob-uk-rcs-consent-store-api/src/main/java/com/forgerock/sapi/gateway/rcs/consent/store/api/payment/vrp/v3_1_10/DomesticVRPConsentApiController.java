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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.vrp.v3_1_10;

import java.util.Objects;
import java.util.function.Supplier;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.vrp.v3_1_10.CreateDomesticVRPConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.vrp.DomesticVRPConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.vrp.DomesticVRPConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;

/**
 * Implementation of DomesticVRPConsentApi for OBIE version 3.1.10
 *
 * Note: the obVersion field is pluggable, so if there are no changes to the OBIE schema in later versions, then
 * these controllers can extend this and configure the
 */
@Controller
public class DomesticVRPConsentApiController implements DomesticVRPConsentApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DomesticVRPConsentService consentService;

    private final Supplier<DateTime> idempotencyKeyExpirationSupplier;

    private final OBVersion obVersion;

    @Autowired
    public DomesticVRPConsentApiController(DomesticVRPConsentService consentService,
                                           Supplier<DateTime> idempotencyKeyExpirationSupplier) {
        this(consentService, idempotencyKeyExpirationSupplier, OBVersion.v3_1_10);
    }

    public DomesticVRPConsentApiController(DomesticVRPConsentService consentService,
                                               Supplier<DateTime> idempotencyKeyExpirationSupplier,
                                               OBVersion obVersion) {
        this.consentService = Objects.requireNonNull(consentService, "consentService must be provided");
        this.idempotencyKeyExpirationSupplier = Objects.requireNonNull(idempotencyKeyExpirationSupplier, "idempotencyKeyExpirationSupplier must be provided");
        this.obVersion = Objects.requireNonNull(obVersion, "obVersion must be provided");
    }

    @Override
    public ResponseEntity<DomesticVRPConsent> createConsent(CreateDomesticVRPConsentRequest request) {
        logger.info("Attempting to createConsent: {}", request);
        final DomesticVRPConsentEntity domesticPaymentConsent = new DomesticVRPConsentEntity();
        domesticPaymentConsent.setRequestVersion(obVersion);
        domesticPaymentConsent.setApiClientId(request.getApiClientId());
        domesticPaymentConsent.setRequestObj(request.getConsentRequest());
        domesticPaymentConsent.setStatus(StatusEnum.AWAITINGAUTHORISATION.toString());
        domesticPaymentConsent.setCharges(request.getCharges());
        domesticPaymentConsent.setIdempotencyKey(request.getIdempotencyKey());
        domesticPaymentConsent.setIdempotencyKeyExpiration(idempotencyKeyExpirationSupplier.get());
        final DomesticVRPConsentEntity persistedEntity = consentService.createConsent(domesticPaymentConsent);
        logger.info("Consent created with id: {}", persistedEntity.getId());

        return new ResponseEntity<>(convertEntityToDto(persistedEntity), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<DomesticVRPConsent> getConsent(String consentId, String apiClientId) {
        logger.info("Attempting to getConsent - id: {}, for apiClientId: {}", consentId, apiClientId);
        return ResponseEntity.ok(convertEntityToDto(consentService.getConsent(consentId, apiClientId)));
    }

    @Override
    public ResponseEntity<DomesticVRPConsent> authoriseConsent(String consentId, AuthorisePaymentConsentRequest request) {
        logger.info("Attempting to authoriseConsent - id: {}, request: {}", consentId, request);
        final PaymentAuthoriseConsentArgs paymentAuthoriseConsentArgs = new PaymentAuthoriseConsentArgs(consentId,
                request.getApiClientId(), request.getResourceOwnerId(), request.getAuthorisedDebtorAccountId());
        return ResponseEntity.ok(convertEntityToDto(consentService.authoriseConsent(paymentAuthoriseConsentArgs)));
    }

    @Override
    public ResponseEntity<DomesticVRPConsent> rejectConsent(String consentId, RejectConsentRequest request) {
        logger.info("Attempting to rejectConsent - id: {}, request: {}", consentId, request);
        return ResponseEntity.ok(convertEntityToDto(consentService.rejectConsent(consentId, request.getApiClientId(), request.getResourceOwnerId())));
    }

    @Override
    public ResponseEntity<Void> deleteConsent(String consentId, String apiClientId) {
        logger.info("Attempting to deleteConsent - id: {}, apiClientId: {}", consentId, apiClientId);
        consentService.deleteConsent(consentId, apiClientId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private DomesticVRPConsent convertEntityToDto(DomesticVRPConsentEntity entity) {
        final DomesticVRPConsent dto = new DomesticVRPConsent();
        dto.setId(entity.getId());
        dto.setStatus(entity.getStatus());
        dto.setRequestObj(entity.getRequestObj());
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
