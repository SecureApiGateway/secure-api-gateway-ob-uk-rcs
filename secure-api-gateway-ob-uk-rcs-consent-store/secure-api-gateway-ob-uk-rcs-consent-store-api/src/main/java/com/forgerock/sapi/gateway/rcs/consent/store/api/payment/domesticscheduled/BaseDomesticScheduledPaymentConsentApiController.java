package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domesticscheduled;

import java.util.Objects;
import java.util.function.Supplier;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.CreateDomesticScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticScheduledPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DomesticScheduledPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;

public class BaseDomesticScheduledPaymentConsentApiController implements DomesticScheduledPaymentConsentApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final DomesticScheduledPaymentConsentService consentService;
    protected final Supplier<DateTime> idempotencyKeyExpirationSupplier;
    protected final OBVersion obVersion;

    public BaseDomesticScheduledPaymentConsentApiController(DomesticScheduledPaymentConsentService consentService,
                                                            Supplier<DateTime> idempotencyKeyExpirationSupplier,
                                                            OBVersion obVersion) {
        this.consentService = Objects.requireNonNull(consentService, "consentService must be provided");
        this.idempotencyKeyExpirationSupplier = Objects.requireNonNull(idempotencyKeyExpirationSupplier, "idempotencyKeyExpirationSupplier must be provided");
        this.obVersion = Objects.requireNonNull(obVersion, "obVersion must be provided");
    }

    @Override
    public ResponseEntity<DomesticScheduledPaymentConsent> createConsent(CreateDomesticScheduledPaymentConsentRequest request) {

        logger.info("Attempting to createConsent: {}", request);
        final DomesticScheduledPaymentConsentEntity domesticScheduledPaymentConsent = new DomesticScheduledPaymentConsentEntity();
        domesticScheduledPaymentConsent.setRequestVersion(obVersion);
        domesticScheduledPaymentConsent.setApiClientId(request.getApiClientId());
        domesticScheduledPaymentConsent.setRequestObj(request.getConsentRequest());
        domesticScheduledPaymentConsent.setStatus(OBPaymentConsentStatus.AWAITINGAUTHORISATION.toString());
        domesticScheduledPaymentConsent.setCharges(request.getCharges());
        domesticScheduledPaymentConsent.setIdempotencyKey(request.getIdempotencyKey());
        domesticScheduledPaymentConsent.setIdempotencyKeyExpiration(idempotencyKeyExpirationSupplier.get());
        final DomesticScheduledPaymentConsentEntity persistedEntity = consentService.createConsent(domesticScheduledPaymentConsent);
        logger.info("Consent created with id: {}", persistedEntity.getId());

        return new ResponseEntity<>(convertEntityToDto(persistedEntity), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<DomesticScheduledPaymentConsent> getConsent(String consentId, String apiClientId) {
        logger.info("Attempting to getConsent - id: {}, for apiClientId: {}", consentId, apiClientId);
        return ResponseEntity.ok(convertEntityToDto(consentService.getConsent(consentId, apiClientId)));
    }

    @Override
    public ResponseEntity<DomesticScheduledPaymentConsent> authoriseConsent(String consentId,
            AuthorisePaymentConsentRequest request) {

        logger.info("Attempting to authoriseConsent - id: {}, request: {}", consentId, request);
        final PaymentAuthoriseConsentArgs paymentAuthoriseConsentArgs = new PaymentAuthoriseConsentArgs(consentId,
                request.getApiClientId(), request.getResourceOwnerId(), request.getAuthorisedDebtorAccountId());

        return ResponseEntity.ok(convertEntityToDto(consentService.authoriseConsent(paymentAuthoriseConsentArgs)));
    }

    @Override
    public ResponseEntity<DomesticScheduledPaymentConsent> rejectConsent(String consentId, RejectConsentRequest request) {
        logger.info("Attempting to rejectConsent - id: {}, request: {}", consentId, request);
        return ResponseEntity.ok(convertEntityToDto(consentService.rejectConsent(consentId, request.getApiClientId(), request.getResourceOwnerId())));
    }

    @Override
    public ResponseEntity<DomesticScheduledPaymentConsent> consumeConsent(String consentId, ConsumePaymentConsentRequest request) {
        logger.info("Attempting to consumeConsent - id: {}, request: {}", consentId, request);
        return ResponseEntity.ok(convertEntityToDto(consentService.consumeConsent(consentId, request.getApiClientId())));
    }

    private DomesticScheduledPaymentConsent convertEntityToDto(DomesticScheduledPaymentConsentEntity entity) {
        final DomesticScheduledPaymentConsent dto = new DomesticScheduledPaymentConsent();
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
