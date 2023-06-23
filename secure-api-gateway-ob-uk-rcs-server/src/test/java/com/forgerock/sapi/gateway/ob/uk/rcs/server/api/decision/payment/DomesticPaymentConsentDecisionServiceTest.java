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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.payment.DomesticPaymentConsentDecisionService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@ExtendWith(MockitoExtension.class)
public
class DomesticPaymentConsentDecisionServiceTest {

    private static final String TEST_AUTHORISED_DEBTOR_ACC_ID = "debtor-acc-1";

    private static final String TEST_API_CLIENT_ID = "api-client-1";
    private static final String TEST_RESOURCE_OWNER_ID = "user-123456";

    @Mock
    private DomesticPaymentConsentService domesticPaymentConsentService;

    @InjectMocks
    private DomesticPaymentConsentDecisionService domesticPaymentConsentDecisionService;

    @Test
    public void testAuthoriseConsent() {
        final String intentId =  IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final ConsentDecisionDeserialized consentDecision = createAuthorisePaymentConsentDecision(TEST_AUTHORISED_DEBTOR_ACC_ID);
        domesticPaymentConsentDecisionService.authoriseConsent(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, consentDecision);

        verify(domesticPaymentConsentService).authoriseConsent(refEq(new DomesticPaymentAuthoriseConsentArgs(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_DEBTOR_ACC_ID)));
        verifyNoMoreInteractions(domesticPaymentConsentService);
    }

    @Test
    public void failToAuthoriseConsentIfDebtorAccountIsMissing() {
        final String intentId =  IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final ConsentDecisionDeserialized decisionWithNullAccountId = new ConsentDecisionDeserialized();
        decisionWithNullAccountId.setDebtorAccount(new FRFinancialAccount());
        final ConsentDecisionDeserialized[] decisionsWithInvalidDebtorAccounts = new ConsentDecisionDeserialized[] {
                new ConsentDecisionDeserialized(),
                decisionWithNullAccountId
        };

        for (ConsentDecisionDeserialized invalidDecision : decisionsWithInvalidDebtorAccounts) {
            final ConsentStoreException consentStoreException = assertThrows(ConsentStoreException.class,
                    () -> domesticPaymentConsentDecisionService.authoriseConsent(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, invalidDecision));
            assertEquals(intentId, consentStoreException.getConsentId());
            assertEquals(ErrorType.INVALID_CONSENT_DECISION, consentStoreException.getErrorType());
        }
    }

    @Test
    public void testRejectConsent() {
        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        domesticPaymentConsentDecisionService.rejectConsent(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID);

        verify(domesticPaymentConsentService).rejectConsent(eq(intentId), eq(TEST_API_CLIENT_ID), eq(TEST_RESOURCE_OWNER_ID));
        verifyNoMoreInteractions(domesticPaymentConsentService);
    }

    public static ConsentDecisionDeserialized createAuthorisePaymentConsentDecision(String debtorAccountId) {
        final ConsentDecisionDeserialized consentDecision = new ConsentDecisionDeserialized();
        consentDecision.setDecision(Constants.ConsentDecisionStatus.AUTHORISED);
        final FRFinancialAccount debtorAccount = new FRFinancialAccount();
        debtorAccount.setAccountId(debtorAccountId);
        consentDecision.setDebtorAccount(debtorAccount);
        return consentDecision;
    }

}