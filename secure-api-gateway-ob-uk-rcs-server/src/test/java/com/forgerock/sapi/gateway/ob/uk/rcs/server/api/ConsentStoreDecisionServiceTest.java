package com.forgerock.sapi.gateway.ob.uk.rcs.server.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreEnabledIntentTypes;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@ExtendWith(MockitoExtension.class)
class ConsentStoreDecisionServiceTest {

    private static final String TEST_API_CLIENT_ID = "api-client-1";
    private static final String TEST_RESOURCE_OWNER_ID = "user-123456";
    private static final String TEST_AUTHORISED_ACC_ID = "authorised-acc-1";
    @Mock
    private ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes;

    @Mock
    private DomesticPaymentConsentService domesticPaymentConsentService;

    @InjectMocks
    private ConsentStoreDecisionService consentStoreDecisionService;

    @Test
    public void testUnableToSubmitDecisionForConsentTypeNotEnabled() {
        final IntentType intentType = IntentType.PAYMENT_DOMESTIC_CONSENT;

        assertEquals("PAYMENT_DOMESTIC_CONSENT not supported", assertThrows(IllegalStateException.class,
                () -> consentStoreDecisionService.authoriseConsent(intentType, intentType.generateIntentId(), TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_ACC_ID)).getMessage());
        assertEquals("PAYMENT_DOMESTIC_CONSENT not supported", assertThrows(IllegalStateException.class,
                () -> consentStoreDecisionService.rejectConsent(intentType, intentType.generateIntentId(), TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID)).getMessage());
    }

    @Test
    public void testSubmitDomesticPaymentAuthoriseDecision() {
        final IntentType paymentDomesticConsent = IntentType.PAYMENT_DOMESTIC_CONSENT;
        given(consentStoreEnabledIntentTypes.isIntentTypeSupported(eq(paymentDomesticConsent))).willReturn(Boolean.TRUE);

        final String intentId = paymentDomesticConsent.generateIntentId();

        consentStoreDecisionService.authoriseConsent(paymentDomesticConsent, intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_ACC_ID);

        verify(domesticPaymentConsentService).authoriseConsent(refEq(new DomesticPaymentAuthoriseConsentArgs(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, TEST_AUTHORISED_ACC_ID)));
        verifyNoMoreInteractions(domesticPaymentConsentService);
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

}