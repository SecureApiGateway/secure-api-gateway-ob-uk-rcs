/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.service.detail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRAccountAccessConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.exception.RcsErrorService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.AccountConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.*;

@Service
@Slf4j
public class AccountConsentDetailsService implements ConsentDetailsService {

    private final AccountConsentService accountConsentService;
    private final TppService tppService;
    private final RcsErrorService rcsErrorService;

    public AccountConsentDetailsService(AccountConsentService accountConsentService,
                                        TppService tppService,
                                        RcsErrorService rcsErrorService) {
        this.accountConsentService = accountConsentService;
        this.tppService = tppService;
        this.rcsErrorService = rcsErrorService;
    }

    @Override
    public AccountsConsentDetails getConsentDetails(ConsentDetailsRequest detailsRequest) throws OBErrorException {
        log.debug("Received an account access consent request with JWT: '{}'", detailsRequest.getConsentRequestJwtString());
        String accountRequestId = detailsRequest.getIntentId();
        log.debug("=> The account detailsRequest id: '{}'", accountRequestId);
        String clientId = detailsRequest.getClientId();
        log.debug("=> The client id: '{}'", clientId);

        FRAccountAccessConsent accountConsent = accountConsentService.getAccountConsent(detailsRequest);
        if (accountConsent == null) {
            log.error("The AISP '{}' is referencing an account detailsRequest {} that doesn't exist", clientId, accountRequestId);
            throw new OBErrorException(RCS_CONSENT_REQUEST_UNKNOWN_ACCOUNT_REQUEST, clientId, accountRequestId);
        }

        // Verify the AISP is the same than the one that created this accountConsent ^
        if (!clientId.equals(accountConsent.getClientId())) {
            log.error("The AISP '{}' created the account detailsRequest '{}' but it's AISP '{}' that is trying to get" +
                    " consent for it.", accountConsent.getClientId(), accountRequestId, clientId);
            throw new OBErrorException(RCS_CONSENT_REQUEST_INVALID_CONSENT, accountConsent.getClientId(), accountRequestId,
                    clientId);
        }

        Optional<Tpp> isTpp = tppService.getTpp(accountConsent.getAispId());
        if (!isTpp.isPresent()) {
            log.error("The TPP '{}' (Client ID {}) that created this consent id '{}' doesn't exist anymore.",
                    accountConsent.getAispId(), clientId, accountConsent.getId());
            throw new OBErrorException(RCS_CONSENT_REQUEST_NOT_FOUND_TPP, clientId, accountConsent.getId());
        }
        Tpp tpp = isTpp.get();

        log.debug("Populate the model with the payment and consent data");
        accountConsent.setUserId(detailsRequest.getUsername());
        accountConsentService.updateAccountConsent(accountConsent);

        log.debug("Populate the model with the payment and consent data");

        return AccountsConsentDetails.builder()
                .permissions(accountConsent.getPermissions())
                .fromTransaction(accountConsent.getTransactionFromDateTime())
                .toTransaction(accountConsent.getTransactionToDateTime())
                .accounts(detailsRequest.getAccounts())
                .username(detailsRequest.getUsername())
                .logo(tpp.getLogoUri())
                .clientId(clientId)
                .aispName(accountConsent.getAispName())
                .expiredDate(accountConsent.getExpirationDateTime())
                .build();
    }
}
