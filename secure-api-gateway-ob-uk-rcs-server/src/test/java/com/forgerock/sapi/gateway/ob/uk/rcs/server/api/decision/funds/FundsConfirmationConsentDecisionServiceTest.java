/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.funds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.FundsConfirmationAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.funds.FundsConfirmationConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

/**
 * Test for {@link FundsConfirmationConsentDecisionService}
 */
@ExtendWith(MockitoExtension.class)
public class FundsConfirmationConsentDecisionServiceTest {

    private static final String AUTHORISED_ACCOUNT_ID = UUID.randomUUID().toString();

    private static final String API_CLIENT_ID = UUID.randomUUID().toString();

    private static final String RESOURCE_OWNER_ID = UUID.randomUUID().toString();
    @Mock
    private FundsConfirmationConsentService fundsConfirmationConsentService;

    @InjectMocks
    private FundsConfirmationConsentDecisionService fundsConfirmationConsentDecisionService;

    @Test
    void shouldAuthoriseFundsConsent() {
        final String intentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        final ConsentDecisionDeserialized consentDecisionDeserialized = createAuthoriseConsentDecision(AUTHORISED_ACCOUNT_ID);
        fundsConfirmationConsentDecisionService.authoriseConsent(
                intentId,
                API_CLIENT_ID,
                RESOURCE_OWNER_ID,
                consentDecisionDeserialized
        );

        verify(fundsConfirmationConsentService).authoriseConsent(
                refEq(
                        new FundsConfirmationAuthoriseConsentArgs(
                                intentId,
                                API_CLIENT_ID,
                                RESOURCE_OWNER_ID,
                                AUTHORISED_ACCOUNT_ID
                        )
                )
        );
        verifyNoMoreInteractions(fundsConfirmationConsentService);
    }

    @Test
    void throwExceptionFundsMissingAuthorisedAccount() {
        final String intentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        final ConsentDecisionDeserialized consentDecisionDeserialized = new ConsentDecisionDeserialized();
        final ConsentStoreException exception = assertThrows(
                ConsentStoreException.class,
                () -> fundsConfirmationConsentDecisionService.authoriseConsent(
                        intentId, API_CLIENT_ID, RESOURCE_OWNER_ID, consentDecisionDeserialized
                )
        );

        assertEquals(ConsentStoreException.ErrorType.INVALID_CONSENT_DECISION, exception.getErrorType());
        assertEquals(intentId, exception.getConsentId());
    }

    @Test
    void shouldSubmitFundsRejectDecision() {
        final String intentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        fundsConfirmationConsentDecisionService.rejectConsent(
                intentId, API_CLIENT_ID, RESOURCE_OWNER_ID
        );
        verify(fundsConfirmationConsentService).rejectConsent(
                eq(intentId),
                eq(API_CLIENT_ID),
                eq(RESOURCE_OWNER_ID)
        );
        verifyNoMoreInteractions(fundsConfirmationConsentService);
    }

    public static ConsentDecisionDeserialized createAuthoriseConsentDecision(String authorisedAccountId) {
        return ConsentDecisionDeserialized.builder()
                .decision(Constants.ConsentDecisionStatus.AUTHORISED)
                .debtorAccount(
                        FRFinancialAccount.builder()
                                .accountId(authorisedAccountId)
                                .build()
                )
                .build();
    }
}
