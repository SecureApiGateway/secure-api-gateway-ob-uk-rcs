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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision.customerinfo;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo.CustomerInfoAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo.CustomerInfoConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Test for {@link CustomerInfoConsentDecisionService}
 */

@ExtendWith(MockitoExtension.class)
public class CustomerInfoConsentDecisionServiceTest {
    private static final String TEST_API_CLIENT_ID = UUID.randomUUID().toString();
    private static final String TEST_RESOURCE_OWNER_ID = UUID.randomUUID().toString();

    @Mock
    private CustomerInfoConsentService customerInfoConsentService;

    @InjectMocks
    private CustomerInfoConsentDecisionService customerInfoConsentDecisionService;

    @Test
    public void testAuthoriseConsent() {
        final String intentId = IntentType.ACCOUNT_ACCESS_CONSENT.generateIntentId();

        final ConsentDecisionDeserialized consentDecision = createAuthoriseCustomerInfoConsentDecision();
        customerInfoConsentDecisionService.authoriseConsent(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID, consentDecision);

        verify(customerInfoConsentService).authoriseConsent(refEq(new CustomerInfoAuthoriseConsentArgs(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID)));
        verifyNoMoreInteractions(customerInfoConsentService);
    }

    @Test
    public void testSubmitCustomerInfoRejectDecision() {
        final String intentId = IntentType.ACCOUNT_ACCESS_CONSENT.generateIntentId();

        customerInfoConsentDecisionService.rejectConsent(intentId, TEST_API_CLIENT_ID, TEST_RESOURCE_OWNER_ID);

        verify(customerInfoConsentService).rejectConsent(eq(intentId), eq(TEST_API_CLIENT_ID), eq(TEST_RESOURCE_OWNER_ID));
        verifyNoMoreInteractions(customerInfoConsentService);
    }

    public static ConsentDecisionDeserialized createAuthoriseCustomerInfoConsentDecision() {
        final ConsentDecisionDeserialized consentDecision = new ConsentDecisionDeserialized();
        consentDecision.setDecision(Constants.ConsentDecisionStatus.AUTHORISED);
        return consentDecision;
    }
}
