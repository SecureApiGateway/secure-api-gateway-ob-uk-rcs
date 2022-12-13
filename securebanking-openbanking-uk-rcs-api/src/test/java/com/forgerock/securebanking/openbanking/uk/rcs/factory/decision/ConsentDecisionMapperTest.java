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
package com.forgerock.securebanking.openbanking.uk.rcs.factory.decision;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.securebanking.openbanking.uk.rcs.mapper.decision.ConsentDecisionMapper;
import com.forgerock.securebanking.platform.client.Constants;
import com.forgerock.securebanking.platform.client.models.ConsentClientDecisionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.ConsentDecisionRequestTestDataFactory.aValidAccountConsentClientDecisionRequest;
import static com.forgerock.securebanking.platform.client.test.support.DomesticPaymentConsentDetailsTestFactory.aValidDomesticPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.DomesticScheduledPaymentConsentDetailsTestFactory.aValidDomesticScheduledPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.DomesticStandingOrderConsentDetailsTestFactory.aValidDomesticStandingOrderConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory.aValidDomesticVrpPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.FilePaymentConsentDetailsTestFactory.aValidFilePaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.InternationalPaymentConsentDetailsTestFactory.aValidInternationalPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.InternationalScheduledPaymentConsentDetailsTestFactory.aValidInternationalScheduledPaymentConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.InternationalStandingOrderConsentDetailsTestFactory.aValidInternationalStandingOrderConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ConsentDecisionMapper} factories
 */
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ConsentDecisionMapper.class,
})
public class ConsentDecisionMapperTest {

    @Autowired
    private ConsentDecisionMapper consentDecisionMapper;

    @Test
    public void shouldMapConsentDecisionDeserializedToConsentClientDecisionRequest() {
        // Given
        ConsentDecisionDeserialized consentDecisionDeserialized =ConsentDecisionDeserialized.builder()
                .accountIds(List.of("account1", "account2"))
                .consentJwt("asfdasdfasdf")
                .decision(Constants.ConsentDecisionStatus.AUTHORISED)
                .build();

        // When
        ConsentClientDecisionRequest consentClientDecisionRequest = consentDecisionMapper.map(consentDecisionDeserialized);

        // Then
        assertThat(consentClientDecisionRequest.getAccountIds()).containsExactlyElementsOf(
                consentDecisionDeserialized.getAccountIds()
        );

        assertThat(consentClientDecisionRequest.getConsentJwt()).isEqualTo(consentDecisionDeserialized.getConsentJwt());
    }
}
