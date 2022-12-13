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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAccountIdentifier;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ConsentDecisionMapper}
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
        FRAccountIdentifier accountIdentifier = new FRAccountIdentifier();
        accountIdentifier.setIdentification("76064512389965");
        accountIdentifier.setName("John");
        accountIdentifier.setSchemeName("UK.OBIE.SortCodeAccountNumber");
        FRFinancialAccount financialAccount = new FRFinancialAccount();
        financialAccount.setAccounts(List.of(accountIdentifier));
        financialAccount.setAccountId("30ff5da7-7d0f-43fe-974c-7b34717cbeec");
        ConsentDecisionDeserialized consentDecisionDeserialized = ConsentDecisionDeserialized.builder()
                .accountIds(List.of("account1", "account2"))
                .consentJwt("asfdasdfasdf")
                .decision(Constants.ConsentDecisionStatus.AUTHORISED)
                .debtorAccount(financialAccount)
                .build();

        // When
        ConsentClientDecisionRequest consentClientDecisionRequest = consentDecisionMapper.map(consentDecisionDeserialized);

        // Then
        assertThat(consentClientDecisionRequest.getAccountIds()).containsExactlyElementsOf(
                consentDecisionDeserialized.getAccountIds()
        );
        assertThat(consentClientDecisionRequest.getData().getDebtorAccount()).isEqualTo(accountIdentifier);
        assertThat(consentClientDecisionRequest.getConsentJwt()).isEqualTo(consentDecisionDeserialized.getConsentJwt());
    }
}
