/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.internationalscheduled;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.CreateInternationalScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.InternationalScheduledPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.InternationalScheduledPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international.InternationalScheduledPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;

/**
 * Base implementation of {@link InternationalScheduledPaymentConsentApi} containing the logic that is common across versions.
 * The {@link OBVersion} can be plugged in via the constructor.
 * <p>
 * Version specific controller implementations should extend this class and specify the following annotations:
 * {@link org.springframework.stereotype.Controller}, {@link org.springframework.web.bind.annotation.RequestMapping}
 * and {@link io.swagger.annotations.Api}.
 */
public class BaseInternationalScheduledPaymentConsentApiController implements InternationalScheduledPaymentConsentApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final InternationalScheduledPaymentConsentService consentService;
    private final Supplier<DateTime> idempotencyKeyExpirationSupplier;
    private final OBVersion obVersion;

    public BaseInternationalScheduledPaymentConsentApiController(InternationalScheduledPaymentConsentService consentService,
                                                                 Supplier<DateTime> idempotencyKeyExpirationSupplier,
                                                                 OBVersion obVersion) {
        this.consentService = requireNonNull(consentService, "consentService must be provided");
        this.idempotencyKeyExpirationSupplier = requireNonNull(idempotencyKeyExpirationSupplier, "idempotencyKeyExpirationSupplier must be provided");
        this.obVersion = requireNonNull(obVersion, "obVersion must be provided");
    }

    @Override
    public ResponseEntity<InternationalScheduledPaymentConsent> createConsent(CreateInternationalScheduledPaymentConsentRequest request) {
        logger.info("Attempting to createConsent: {}", request);
        final InternationalScheduledPaymentConsentEntity internationalPaymentConsent = new InternationalScheduledPaymentConsentEntity();
        internationalPaymentConsent.setRequestVersion(obVersion);
        internationalPaymentConsent.setApiClientId(request.getApiClientId());
        internationalPaymentConsent.setRequestObj(request.getConsentRequest());
        internationalPaymentConsent.setStatus(OBPaymentConsentStatus.AWAITINGAUTHORISATION.toString());
        internationalPaymentConsent.setCharges(request.getCharges());
        internationalPaymentConsent.setIdempotencyKey(request.getIdempotencyKey());
        internationalPaymentConsent.setIdempotencyKeyExpiration(idempotencyKeyExpirationSupplier.get());
        internationalPaymentConsent.setExchangeRateInformation(request.getExchangeRateInformation());
        final InternationalScheduledPaymentConsentEntity persistedEntity = consentService.createConsent(internationalPaymentConsent);
        logger.info("Consent created with id: {}", persistedEntity.getId());

        return new ResponseEntity<>(convertEntityToDto(persistedEntity), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<InternationalScheduledPaymentConsent> getConsent(String consentId, String apiClientId) {
        logger.info("Attempting to getConsent - id: {}, for apiClientId: {}", consentId, apiClientId);
        return ResponseEntity.ok(convertEntityToDto(consentService.getConsent(consentId, apiClientId)));
    }

    @Override
    public ResponseEntity<InternationalScheduledPaymentConsent> authoriseConsent(String consentId, AuthorisePaymentConsentRequest request) {
        logger.info("Attempting to authoriseConsent - id: {}, request: {}", consentId, request);
        final PaymentAuthoriseConsentArgs paymentAuthoriseConsentArgs = new PaymentAuthoriseConsentArgs(consentId,
                request.getApiClientId(), request.getResourceOwnerId(), request.getAuthorisedDebtorAccountId());
        return ResponseEntity.ok(convertEntityToDto(consentService.authoriseConsent(paymentAuthoriseConsentArgs)));
    }

    @Override
    public ResponseEntity<InternationalScheduledPaymentConsent> rejectConsent(String consentId, RejectConsentRequest request) {
        logger.info("Attempting to rejectConsent - id: {}, request: {}", consentId, request);
        return ResponseEntity.ok(convertEntityToDto(consentService.rejectConsent(consentId, request.getApiClientId(), request.getResourceOwnerId())));
    }

    @Override
    public ResponseEntity<InternationalScheduledPaymentConsent> consumeConsent(String consentId, ConsumePaymentConsentRequest request) {
        logger.info("Attempting to consumeConsent - id: {}, request: {},", consentId, request);
        return ResponseEntity.ok(convertEntityToDto(consentService.consumeConsent(consentId, request.getApiClientId())));
    }

    private InternationalScheduledPaymentConsent convertEntityToDto(InternationalScheduledPaymentConsentEntity entity) {
        final InternationalScheduledPaymentConsent dto = new InternationalScheduledPaymentConsent();
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
        dto.setExchangeRateInformation(entity.getExchangeRateInformation());
        dto.setCreationDateTime(entity.getCreationDateTime());
        dto.setStatusUpdateDateTime(entity.getStatusUpdatedDateTime());
        return dto;
    }
}
