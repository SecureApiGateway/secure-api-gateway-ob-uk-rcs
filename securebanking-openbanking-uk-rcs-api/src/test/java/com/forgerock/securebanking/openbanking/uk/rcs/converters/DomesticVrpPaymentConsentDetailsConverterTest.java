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
package com.forgerock.securebanking.openbanking.uk.rcs.converters;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticVrpPaymentConsentDetails;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Instant;
import org.junit.jupiter.api.Test;
import uk.org.openbanking.datamodel.common.OBVRPConsentType;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.DOMESTIC_VRP_INTENT_ID;
import static com.forgerock.securebanking.platform.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory.aValidDomesticVrpPaymentConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DomesticVrpPaymentConsentDetailsConverter}
 */
@Slf4j
public class DomesticVrpPaymentConsentDetailsConverterTest {

    @Test
    public void shouldConvertConsentDetailsToDomesticVrpPaymentConsentDetailsSweeping() {
        // Given
        JsonObject consentDetails = aValidDomesticVrpPaymentConsentDetails(DOMESTIC_VRP_INTENT_ID, OBVRPConsentType.SWEEPING);

        // When
        DomesticVrpPaymentConsentDetails domesticVrpPaymentConsentDetails =
                DomesticVrpPaymentConsentDetailsConverter.getInstance().toDomesticVrpPaymentConsentDetails(consentDetails);

        // Then
        JsonObject data = consentDetails.getAsJsonObject(OB_INTENT_OBJECT).getAsJsonObject(DATA);
        JsonObject initiation = data.getAsJsonObject(INITIATION);
        JsonObject controlParameters = data.getAsJsonObject(CONTROL_PARAMETERS);

        assertThat(domesticVrpPaymentConsentDetails.getInitiation().getCreditorAccount().getIdentification())
                .isEqualTo(initiation.getAsJsonObject(CREDITOR_ACCOUNT).get(IDENTIFICATION).getAsString());

        assertThat(domesticVrpPaymentConsentDetails.getInitiation().getDebtorAccount().getIdentification())
                .isEqualTo(initiation.getAsJsonObject(DEBTOR_ACCOUNT).get(IDENTIFICATION).getAsString());

        assertThat(domesticVrpPaymentConsentDetails.getControlParameters().getValidFromDateTime())
                .isEqualTo(Instant.parse(controlParameters.get(VALID_FROM_DATETIME).getAsString()).toDateTime());

        assertThat(domesticVrpPaymentConsentDetails.getControlParameters().getValidToDateTime())
                .isEqualTo(Instant.parse(controlParameters.get(VALID_TO_DATETIME).getAsString()).toDateTime());

        assertThat(domesticVrpPaymentConsentDetails.getControlParameters().getVrPType()).containsExactly(
                OBVRPConsentType.SWEEPING.getValue()
        );

        assertThat(domesticVrpPaymentConsentDetails.getControlParameters().getPeriodicLimits()).isNotNull().isNotEmpty();

    }

}
