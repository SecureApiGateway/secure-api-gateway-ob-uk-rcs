package com.forgerock.sapi.gateway.rcs.consent.store.api.account;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AuthoriseAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.common.OBExternalRequestStatus1Code;

public class BaseAccountAccessConsentApiController implements AccountAccessConsentApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AccountAccessConsentService consentService;
    private final OBVersion obVersion;

    public BaseAccountAccessConsentApiController(AccountAccessConsentService consentService, OBVersion obVersion) {
        this.consentService = Objects.requireNonNull(consentService, "consentService must be provided");
        this.obVersion = Objects.requireNonNull(obVersion, "obVersion must be provided");
    }

    @Override
    public ResponseEntity<AccountAccessConsent> createConsent(CreateAccountAccessConsentRequest request) {
        logger.info("Attempting to createConsent: {}", request);
        final AccountAccessConsentEntity consentEntity = new AccountAccessConsentEntity();
        consentEntity.setRequestObj(request.getConsentRequest());
        consentEntity.setApiClientId(request.getApiClientId());
        consentEntity.setRequestVersion(obVersion);
        consentEntity.setStatus(OBExternalRequestStatus1Code.AWAITINGAUTHORISATION.toString());

        final AccountAccessConsentEntity persistedEntity = consentService.createConsent(consentEntity);
        logger.info("Consent created with id: {}", persistedEntity.getId());

        return new ResponseEntity<>(convertEntityToDto(persistedEntity), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<AccountAccessConsent> getConsent(String consentId, String apiClientId) {
        logger.info("Attempting to getConsent - id: {}, for apiClientId: {}", consentId, apiClientId);
        return ResponseEntity.ok(convertEntityToDto(consentService.getConsent(consentId, apiClientId)));
    }

    @Override
    public ResponseEntity<AccountAccessConsent> authoriseConsent(String consentId, AuthoriseAccountAccessConsentRequest request) {
        logger.info("Attempting to authoriseConsent - id: {}, request: {}", consentId, request);
        final AccountAccessAuthoriseConsentArgs authoriseArgs = new AccountAccessAuthoriseConsentArgs(consentId, request.getApiClientId(),
                request.getResourceOwnerId(), request.getAuthorisedAccountIds());
        return ResponseEntity.ok(convertEntityToDto(consentService.authoriseConsent(authoriseArgs)));
    }

    @Override
    public ResponseEntity<AccountAccessConsent> rejectConsent(String consentId, RejectConsentRequest request) {
        logger.info("Attempting to rejectConsent - id: {}, request: {}", consentId, request);
        return ResponseEntity.ok(convertEntityToDto(consentService.rejectConsent(consentId, request.getApiClientId(), request.getResourceOwnerId())));
    }

    @Override
    public ResponseEntity<Void> deleteConsent(String consentId, String apiClientId) {
        logger.info("Attempting to deleteConsent - id: {}, apiClientId: {}", consentId, apiClientId);
        consentService.deleteConsent(consentId, apiClientId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private AccountAccessConsent convertEntityToDto(AccountAccessConsentEntity entity) {
        final AccountAccessConsent dto = new AccountAccessConsent();
        dto.setApiClientId(entity.getApiClientId());
        dto.setAuthorisedAccountIds(entity.getAuthorisedAccountIds());
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
