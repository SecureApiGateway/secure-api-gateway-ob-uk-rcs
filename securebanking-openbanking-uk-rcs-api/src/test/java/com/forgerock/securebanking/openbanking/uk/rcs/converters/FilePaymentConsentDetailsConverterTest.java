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

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.FilePaymentConsentDetails;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Instant;
import org.junit.jupiter.api.Test;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.intent.members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter4Test.FILE_PAYMENT_INTENT_ID;
import static com.forgerock.securebanking.platform.client.test.support.FilePaymentConsentDetailsTestFactory.aValidFilePaymentConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link FilePaymentConsentDetailsConverter}
 */
@Slf4j
public class FilePaymentConsentDetailsConverterTest {
    @Test
    public void shouldConvertConsentDetailsToFilePaymentConsentDetails() {
        // Given
        JsonObject consentDetails = aValidFilePaymentConsentDetails(FILE_PAYMENT_INTENT_ID);

        // When
        FilePaymentConsentDetails filePaymentConsentDetails = FilePaymentConsentDetailsConverter.getInstance().toFilePaymentConsentDetails(consentDetails);

        // Then
        JsonObject data = consentDetails.getAsJsonObject(OB_INTENT_OBJECT).getAsJsonObject(DATA);
        JsonObject initiation = data.getAsJsonObject(INITIATION);

        assertThat(filePaymentConsentDetails.getFilePayment().getNumberOfTransactions())
                .isEqualTo(initiation.get(NUMBER_OF_TRANSACTIONS).getAsString());

        assertThat(filePaymentConsentDetails.getFilePayment().getControlSum())
                .isEqualTo(initiation.get(CONTROL_SUM).getAsBigDecimal());

        assertThat(filePaymentConsentDetails.getFilePayment().getRequestedExecutionDateTime())
                .isEqualTo(Instant.parse(initiation.get(REQUESTED_EXECUTION_DATETIME).getAsString()).toDateTime());

        assertThat(filePaymentConsentDetails.getFilePayment().getFileReference())
                .isEqualTo(initiation.get(FILE_REFERENCE).getAsString());

        assertThat(filePaymentConsentDetails.getCharges())
                .isNotNull();
    }
}
