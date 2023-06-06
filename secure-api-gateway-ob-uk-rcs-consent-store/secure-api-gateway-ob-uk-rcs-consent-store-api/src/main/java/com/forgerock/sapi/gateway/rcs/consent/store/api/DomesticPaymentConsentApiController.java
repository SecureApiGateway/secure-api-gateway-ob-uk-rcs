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
package com.forgerock.sapi.gateway.rcs.consent.store.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.AuthoriseDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.ConsumeDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.RejectDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;

@Controller
public class DomesticPaymentConsentApiController implements DomesticPaymentConsentApi {


    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DomesticPaymentConsentService consentService;

    @Autowired
    public DomesticPaymentConsentApiController(DomesticPaymentConsentService consentService) {
        this.consentService = consentService;
    }

    @Override
    public ResponseEntity<DomesticPaymentConsent> createConsent(CreateDomesticPaymentConsentRequest request, String apiClientId) {
        final DomesticPaymentConsentEntity domesticPaymentConsent = new DomesticPaymentConsentEntity();
        domesticPaymentConsent.setRequestType(request.getConsentRequest().getClass().getSimpleName());
        domesticPaymentConsent.setRequestVersion(OBVersion.v3_1_10);
        domesticPaymentConsent.setApiClientId(request.getApiClientId());
        domesticPaymentConsent.setRequestObj(request.getConsentRequest());
        domesticPaymentConsent.setStatus(StatusEnum.AWAITINGAUTHORISATION.toString());
        domesticPaymentConsent.setCharges(request.getCharges());
        domesticPaymentConsent.setIdempotencyKey(request.getIdempotencyKey());
        domesticPaymentConsent.setIdempotencyKeyExpiration(request.getIdempotencyKeyExpiration());
        final DomesticPaymentConsentEntity persistedEntity = consentService.createConsent(domesticPaymentConsent);

        return new ResponseEntity<>(convertEntityToDto(persistedEntity), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<DomesticPaymentConsent> getConsent(String consentId, String apiClientId) {
        return ResponseEntity.ok(convertEntityToDto(consentService.getConsent(consentId, apiClientId)));
    }

    @Override
    public ResponseEntity<DomesticPaymentConsent> authoriseConsent(String consentId, AuthoriseDomesticPaymentConsentRequest request, String apiClientId) {
        // TODO cleanup
        final DomesticPaymentAuthoriseConsentArgs domesticPaymentAuthoriseConsentArgs = new DomesticPaymentAuthoriseConsentArgs(consentId, request.getResourceOwnerId(), apiClientId, request.getAuthorisedDebtorAccountId());
        return ResponseEntity.ok(convertEntityToDto(consentService.authoriseConsent(domesticPaymentAuthoriseConsentArgs)));
    }

    @Override
    public ResponseEntity<DomesticPaymentConsent> rejectConsent(String consentId, RejectDomesticPaymentConsentRequest request, String apiClientId) {
        return ResponseEntity.ok(convertEntityToDto(consentService.rejectConsent(consentId, apiClientId, request.getResourceOwnerId())));
    }

    @Override
    public ResponseEntity<DomesticPaymentConsent> consumeConsent(String consentId, ConsumeDomesticPaymentConsentRequest request, String apiClientId) {
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
        return dto;
    }
}
