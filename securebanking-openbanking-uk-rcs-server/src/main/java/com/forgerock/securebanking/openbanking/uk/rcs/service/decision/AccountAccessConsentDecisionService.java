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
package com.forgerock.securebanking.openbanking.uk.rcs.service.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRAccountAccessConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.AccountConsentDecision;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.AccountConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.rs.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode.AUTHORISED;
import static com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRConsentStatusCode.REJECTED;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_DECISION_INVALID_ACCOUNT;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_REQUEST_UNKNOWN_ACCOUNT_REQUEST;
import static com.forgerock.securebanking.openbanking.uk.rcs.util.ConsentDecisionDeserializer.deserializeConsentDecision;

@Service
@Slf4j
public class AccountAccessConsentDecisionService implements ConsentDecisionService {

    private final AccountService accountService;
    private final AccountConsentService accountConsentService;
    private final ObjectMapper objectMapper;

    public AccountAccessConsentDecisionService(AccountService accountService,
                                               AccountConsentService accountConsentService,
                                               ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.accountConsentService = accountConsentService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void processConsentDecision(String intentId, String consentDecisionSerialised, boolean decision) throws OBErrorException {
        FRAccountAccessConsent accountAccessRequest = getAccountConsent(intentId);
        AccountConsentDecision accountConsentDecision = deserializeConsentDecision(consentDecisionSerialised,
                objectMapper, AccountConsentDecision.class);

        if (decision) {
            List<FRAccountWithBalance> accounts = accountService.getAccountsWithBalance(accountAccessRequest.getUserId());
            List<String> accountsId = accounts.stream().map(FRAccountWithBalance::getId).collect(Collectors.toList());
            if (!accountsId.containsAll(accountConsentDecision.getSharedAccounts())) {
                log.error("The PSU {} is trying to share an account '{}' they do not own. List of their accounts '{}'",
                        accountAccessRequest.getUserId(),
                        accountsId,
                        accountConsentDecision.getSharedAccounts());
                throw new OBErrorException(RCS_CONSENT_DECISION_INVALID_ACCOUNT,
                        accountAccessRequest.getUserId(),
                        accountsId,
                        accountConsentDecision.getSharedAccounts());
            }
            accountAccessRequest.setAccountIds(accountConsentDecision.getSharedAccounts());
            accountAccessRequest.setStatus(AUTHORISED);
        } else {
            log.debug("The account request {} has been deny", accountAccessRequest.getId());
            accountAccessRequest.setStatus(REJECTED);
        }
        accountConsentService.updateAccountConsent(accountAccessRequest);
    }

    @Override
    public String getTppIdBehindConsent(String intentId) throws OBErrorException {
        return getAccountConsent(intentId).getAispId();
    }

    @Override
    public String getUserIdBehindConsent(String intentId) throws OBErrorException {
        return getAccountConsent(intentId).getUserId();
    }

    private FRAccountAccessConsent getAccountConsent(String intentId) throws OBErrorException {
        FRAccountAccessConsent accountRequest = accountConsentService.getAccountConsent(intentId);
        if (accountRequest == null) {
            log.error("The AISP is referencing an account request {} that doesn't exist", intentId);
            throw new OBErrorException(RCS_CONSENT_REQUEST_UNKNOWN_ACCOUNT_REQUEST, intentId);
        }
        return accountRequest;
    }
}
