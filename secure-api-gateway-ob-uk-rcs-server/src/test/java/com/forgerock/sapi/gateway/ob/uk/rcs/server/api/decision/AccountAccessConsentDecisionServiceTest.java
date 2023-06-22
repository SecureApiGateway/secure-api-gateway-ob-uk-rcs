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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@ExtendWith(MockitoExtension.class)
class AccountAccessConsentDecisionServiceTest {

    private static final List<String> TEST_AUTHORISED_ACCOUNT_ACCESS_IDS = List.of("acc-1", "acc-2");
    private static final String TEST_API_CLIENT_ID = "test-client-1";
    private static final String TEST_RESOURCE_OWNER_ID = "psu4test";

    @Mock
    private AccountAccessConsentService accountAccessConsentService;

    @InjectMocks
    private AccountAccessConsentDecisionService accountAccessConsentDecisionService;

    @Test
    public void testAuthoriseConsent() {
        final String intentId = IntentType.ACCOUNT_ACCESS_CONSENT.generateIntentId();

        final ConsentDecisionDeserialized consentDecision = createAuthoriseAccountAccessConsentDecision(TEST_AUTHORISED_ACCOUNT_ACCESS_IDS);
        accountAccessConsentDecisionService.authoriseConsent( intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, consentDecision);

        verify(accountAccessConsentService).authoriseConsent(refEq(new AccountAccessAuthoriseConsentArgs(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_ACCOUNT_ACCESS_IDS)));
        verifyNoMoreInteractions(accountAccessConsentService);
    }

    @Test
    public void failToAuthoriseConsentMissingAccountIds() {
        final String intentId = IntentType.ACCOUNT_ACCESS_CONSENT.generateIntentId();

        final ConsentDecisionDeserialized consentDecision = new ConsentDecisionDeserialized();
        final ConsentStoreException consentStoreException = assertThrows(ConsentStoreException.class,
                () -> accountAccessConsentDecisionService.authoriseConsent(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, consentDecision));
        assertEquals(ErrorType.INVALID_CONSENT_DECISION, consentStoreException.getErrorType());
        assertEquals(intentId, consentStoreException.getConsentId());
    }


    @Test
    public void testSubmitAccountAccessRejectDecision() {
        final String intentId = IntentType.ACCOUNT_ACCESS_CONSENT.generateIntentId();

        accountAccessConsentDecisionService.rejectConsent(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID);

        verify(accountAccessConsentService).rejectConsent(eq(intentId), eq(TEST_API_CLIENT_ID), eq(TEST_RESOURCE_OWNER_ID));
        verifyNoMoreInteractions(accountAccessConsentService);
    }

    public static ConsentDecisionDeserialized createAuthoriseAccountAccessConsentDecision(List<String> authorisedAccountIds) {
        final ConsentDecisionDeserialized consentDecision = new ConsentDecisionDeserialized();
        consentDecision.setDecision(Constants.ConsentDecisionStatus.AUTHORISED);
        consentDecision.setAccountIds(authorisedAccountIds);
        return consentDecision;
    }
}