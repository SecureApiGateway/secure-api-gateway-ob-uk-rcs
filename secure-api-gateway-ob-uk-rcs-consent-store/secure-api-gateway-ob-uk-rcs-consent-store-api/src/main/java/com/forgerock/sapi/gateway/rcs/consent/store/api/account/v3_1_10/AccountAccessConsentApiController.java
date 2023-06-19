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
package com.forgerock.sapi.gateway.rcs.consent.store.api.account.v3_1_10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AuthoriseAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.RejectAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;

@Controller
public class AccountAccessConsentApiController implements AccountAccessConsentApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AccountAccessConsentService consentService;

    private final OBVersion obVersion;

    @Autowired
    public AccountAccessConsentApiController(AccountAccessConsentService consentService) {
        this(consentService, OBVersion.v3_1_10);
    }

    public AccountAccessConsentApiController(AccountAccessConsentService consentService, OBVersion obVersion) {
        this.consentService = consentService;
        this.obVersion = obVersion;
    }

    @Override
    public ResponseEntity<AccountAccessConsent> createConsent(CreateAccountAccessConsentRequest request, String apiClientId) {
        logger.info("Attempting to createConsent: {}, for apiClientId: {}", request, apiClientId);
        final AccountAccessConsentEntity consentEntity = new AccountAccessConsentEntity();
        consentEntity.setRequestObj(request.getConsentRequest());
        consentEntity.setApiClientId(apiClientId);
        consentEntity.setRequestVersion(obVersion);
        consentEntity.setRequestType(request.getConsentRequest().getClass().getSimpleName());
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
    public ResponseEntity<AccountAccessConsent> authoriseConsent(String consentId, AuthoriseAccountAccessConsentRequest request, String apiClientId) {
        logger.info("Attempting to authoriseConsent - id: {}, request: {}, apiClientId: {}", consentId, request, apiClientId);
        final AccountAccessAuthoriseConsentArgs authoriseArgs = new AccountAccessAuthoriseConsentArgs(consentId, apiClientId,
                request.getResourceOwnerId(), request.getAuthorisedAccountIds());
        return ResponseEntity.ok(convertEntityToDto(consentService.authoriseConsent(authoriseArgs)));
    }

    @Override
    public ResponseEntity<AccountAccessConsent> rejectConsent(String consentId, RejectAccountAccessConsentRequest request, String apiClientId) {
        logger.info("Attempting to rejectConsent - id: {}, request: {}, apiClientId: {}", consentId, request, apiClientId);
        return ResponseEntity.ok(convertEntityToDto(consentService.rejectConsent(consentId, apiClientId, request.getResourceOwnerId())));
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
        dto.setRequestType(entity.getRequestType());
        dto.setResourceOwnerId(entity.getResourceOwnerId());
        return dto;
    }
}
