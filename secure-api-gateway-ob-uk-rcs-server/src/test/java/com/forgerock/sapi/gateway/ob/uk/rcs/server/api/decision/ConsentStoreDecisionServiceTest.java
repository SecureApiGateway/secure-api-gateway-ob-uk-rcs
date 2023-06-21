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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.verifyNoMoreInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreEnabledIntentTypes;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@ExtendWith(MockitoExtension.class)
class ConsentStoreDecisionServiceTest {

    private static final String TEST_API_CLIENT_ID = "api-client-1";
    private static final String TEST_RESOURCE_OWNER_ID = "user-123456";
    private static final String TEST_AUTHORISED_DEBTOR_ACC_ID = "debtor-acc-1";
    private  static final List<String> TEST_AUTHORISED_ACCOUNT_ACCESS_IDS = List.of("acc-1", "acc-2");
    @Mock
    private ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes;

    @Mock
    private DomesticPaymentConsentService domesticPaymentConsentService;

    @Mock
    private AccountAccessConsentService accountAccessConsentService;

    @InjectMocks
    private ConsentStoreDecisionService consentStoreDecisionService;

    @Test
    public void testUnableToSubmitDecisionForConsentTypeNotEnabled() {
        final IntentType intentType = IntentType.PAYMENT_DOMESTIC_CONSENT;

        assertEquals("PAYMENT_DOMESTIC_CONSENT not supported", assertThrows(IllegalStateException.class,
                () -> consentStoreDecisionService.authoriseConsent(intentType, intentType.generateIntentId(), TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, createAuthorisePaymentConsentDecision())).getMessage());
        assertEquals("PAYMENT_DOMESTIC_CONSENT not supported", assertThrows(IllegalStateException.class,
                () -> consentStoreDecisionService.rejectConsent(intentType, intentType.generateIntentId(), TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID)).getMessage());
    }

    @Test
    public void testSubmitDomesticPaymentAuthoriseDecision() {
        final IntentType paymentDomesticConsent = IntentType.PAYMENT_DOMESTIC_CONSENT;
        given(consentStoreEnabledIntentTypes.isIntentTypeSupported(eq(paymentDomesticConsent))).willReturn(Boolean.TRUE);

        final String intentId = paymentDomesticConsent.generateIntentId();

        final ConsentDecisionDeserialized consentDecision = createAuthorisePaymentConsentDecision();
        consentStoreDecisionService.authoriseConsent(paymentDomesticConsent, intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, consentDecision);

        verify(domesticPaymentConsentService).authoriseConsent(refEq(new DomesticPaymentAuthoriseConsentArgs(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_DEBTOR_ACC_ID)));
        verifyNoMoreInteractions(domesticPaymentConsentService);
    }

    @Test
    public void testSubmitAccountAccessAuthoriseDecision() {
        final IntentType accountAccessConsent = IntentType.ACCOUNT_ACCESS_CONSENT;
        given(consentStoreEnabledIntentTypes.isIntentTypeSupported(eq(accountAccessConsent))).willReturn(Boolean.TRUE);

        final String intentId = accountAccessConsent.generateIntentId();

        final ConsentDecisionDeserialized consentDecision = createAuthoriseAccountAccessConsentDecision();
        consentStoreDecisionService.authoriseConsent(accountAccessConsent, intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, consentDecision);

        verify(accountAccessConsentService).authoriseConsent(refEq(new AccountAccessAuthoriseConsentArgs(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_ACCOUNT_ACCESS_IDS)));
        verifyNoMoreInteractions(accountAccessConsentService);
    }

    private static ConsentDecisionDeserialized createAuthorisePaymentConsentDecision() {
        final ConsentDecisionDeserialized consentDecision = new ConsentDecisionDeserialized();
        consentDecision.setDecision(Constants.ConsentDecisionStatus.AUTHORISED);
        final FRFinancialAccount debtorAccount = new FRFinancialAccount();
        debtorAccount.setAccountId(TEST_AUTHORISED_DEBTOR_ACC_ID);
        consentDecision.setDebtorAccount(debtorAccount);
        return consentDecision;
    }

    private static ConsentDecisionDeserialized createAuthoriseAccountAccessConsentDecision() {
        final ConsentDecisionDeserialized consentDecision = new ConsentDecisionDeserialized();
        consentDecision.setDecision(Constants.ConsentDecisionStatus.AUTHORISED);
        consentDecision.setAccountIds(TEST_AUTHORISED_ACCOUNT_ACCESS_IDS);
        return consentDecision;
    }

    @Test
    public void testSubmitDomesticPaymentRejectDecision() {
        final IntentType paymentDomesticConsent = IntentType.PAYMENT_DOMESTIC_CONSENT;
        given(consentStoreEnabledIntentTypes.isIntentTypeSupported(eq(paymentDomesticConsent))).willReturn(Boolean.TRUE);

        final String intentId = paymentDomesticConsent.generateIntentId();

        consentStoreDecisionService.rejectConsent(paymentDomesticConsent, intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID);

        verify(domesticPaymentConsentService).rejectConsent(eq(intentId), eq(TEST_API_CLIENT_ID), eq(TEST_RESOURCE_OWNER_ID));
        verifyNoMoreInteractions(domesticPaymentConsentService);
    }

    // TODO Review making reject tests use arguments
    @Test
    public void testSubmitAccountAccessRejectDecision() {
        final IntentType accessConsent = IntentType.ACCOUNT_ACCESS_CONSENT;
        given(consentStoreEnabledIntentTypes.isIntentTypeSupported(eq(accessConsent))).willReturn(Boolean.TRUE);

        final String intentId = accessConsent.generateIntentId();

        consentStoreDecisionService.rejectConsent(accessConsent, intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID);

        verify(accountAccessConsentService).rejectConsent(eq(intentId), eq(TEST_API_CLIENT_ID), eq(TEST_RESOURCE_OWNER_ID));
        verifyNoMoreInteractions(domesticPaymentConsentService);
    }
}