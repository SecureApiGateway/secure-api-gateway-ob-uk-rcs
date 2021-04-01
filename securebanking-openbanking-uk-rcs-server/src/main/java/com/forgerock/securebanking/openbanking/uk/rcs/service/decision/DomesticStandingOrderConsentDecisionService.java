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
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.PaymentConsentService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRDomesticStandingOrderConsent;
import org.springframework.stereotype.Service;

@Service
public class DomesticStandingOrderConsentDecisionService extends PaymentConsentDecisionService {

    public DomesticStandingOrderConsentDecisionService(PaymentConsentService paymentConsentService,
                                                       ObjectMapper objectMapper,
                                                       PaymentConsentDecisionUpdater paymentConsentDecisionUpdater) {
        super(paymentConsentService, objectMapper, paymentConsentDecisionUpdater);
    }

    @Override
    protected Class<FRDomesticStandingOrderConsent> getConsentClass() {
        return FRDomesticStandingOrderConsent.class;
    }
}
