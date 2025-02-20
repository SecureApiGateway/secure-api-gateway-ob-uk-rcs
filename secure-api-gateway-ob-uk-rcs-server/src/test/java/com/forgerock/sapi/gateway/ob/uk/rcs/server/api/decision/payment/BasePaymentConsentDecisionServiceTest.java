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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.BasePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

public abstract class BasePaymentConsentDecisionServiceTest<T extends BasePaymentConsentEntity> {
    private static final String TEST_AUTHORISED_DEBTOR_ACC_ID = "debtor-acc-1";
    private static final String TEST_API_CLIENT_ID = "api-client-1";
    private static final String TEST_RESOURCE_OWNER_ID = "user-123456";

    public static ConsentDecisionDeserialized createAuthorisePaymentConsentDecision(String debtorAccountId) {
        final ConsentDecisionDeserialized consentDecision = new ConsentDecisionDeserialized();
        consentDecision.setDecision(Constants.ConsentDecisionStatus.AUTHORISED);
        final FRFinancialAccount debtorAccount = new FRFinancialAccount();
        debtorAccount.setAccountId(debtorAccountId);
        consentDecision.setDebtorAccount(debtorAccount);
        return consentDecision;
    }

    protected abstract ConsentService<T, PaymentAuthoriseConsentArgs> getPaymentConsentService();

    protected abstract BasePaymentConsentDecisionService<T> getConsentDecisionService();

    @Test
    public void testAuthoriseConsent() {
        final String intentId =  IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        final ConsentDecisionDeserialized consentDecision = BasePaymentConsentDecisionServiceTest.createAuthorisePaymentConsentDecision(TEST_AUTHORISED_DEBTOR_ACC_ID);
        getConsentDecisionService().authoriseConsent(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, consentDecision);

        final ConsentService<T, PaymentAuthoriseConsentArgs> paymentConsentService = getPaymentConsentService();
        verify(paymentConsentService).authoriseConsent(refEq(new PaymentAuthoriseConsentArgs(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_DEBTOR_ACC_ID)));
        Mockito.verifyNoMoreInteractions(paymentConsentService);
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
                    () -> getConsentDecisionService().authoriseConsent(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, invalidDecision));
            assertEquals(intentId, consentStoreException.getConsentId());
            assertEquals(ErrorType.INVALID_CONSENT_DECISION, consentStoreException.getErrorType());
        }
    }

    @Test
    public void testRejectConsent() {
        final String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        getConsentDecisionService().rejectConsent(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID);

        verify(getPaymentConsentService()).rejectConsent(eq(intentId), eq(TEST_API_CLIENT_ID), eq(TEST_RESOURCE_OWNER_ID));
        Mockito.verifyNoMoreInteractions(getPaymentConsentService());
    }
}
