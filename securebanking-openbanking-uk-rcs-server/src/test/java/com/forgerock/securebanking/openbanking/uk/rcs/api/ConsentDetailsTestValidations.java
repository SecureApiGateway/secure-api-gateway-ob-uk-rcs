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
package com.forgerock.securebanking.openbanking.uk.rcs.api;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.forgerock.FRFrequency;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticScheduledPaymentConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticStandingOrderConsentDetails;
import com.google.gson.JsonObject;
import org.joda.time.Instant;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsentDetailsTestValidations {

    protected static void validateDomesticScheduledConsentDetailsResponse(
            JsonObject consentDetails,
            DomesticScheduledPaymentConsentDetails responseDetails
    ) {
        assertThat(responseDetails.getPaymentDate()
                .isEqual(
                        Instant.parse(
                                consentDetails.getAsJsonObject(OB_INTENT_OBJECT)
                                        .getAsJsonObject(DATA)
                                        .getAsJsonObject(INITIATION)
                                        .get(REQUESTED_EXECUTION_DATETIME).getAsString()
                        ).toDateTime()
                )
        );
    }

    protected static void validateDomesticStandingOrderConsentDetailsResponse(
            JsonObject consentDetails,
            DomesticStandingOrderConsentDetails responseDetails
    ) {
        final JsonObject expectedIntentData = consentDetails.getAsJsonObject(OB_INTENT_OBJECT).getAsJsonObject(DATA);
        final JsonObject initiation = expectedIntentData.getAsJsonObject(INITIATION);

        assertThat(responseDetails.getInitiation().getFinalPaymentDateTime()
                .isEqual(Instant.parse(initiation.get(FINAL_PAYMENT_DATETIME).getAsString()).toDateTime())
        );
        assertThat(responseDetails.getInitiation().getFirstPaymentDateTime()
                .isEqual(Instant.parse(initiation.get(FIRST_PAYMENT_DATETIME).getAsString()).toDateTime())
        );

        assertThat(responseDetails.getInitiation().getRecurringPaymentDateTime()
                .isEqual(Instant.parse(initiation.get(RECURRING_PAYMENT_DATETIME).getAsString()).toDateTime())
        );

        assertThat(responseDetails.getInitiation().getFrequency())
                .isEqualTo((new FRFrequency(initiation.get(FREQUENCY).getAsString())).getSentence());

        assertThat(responseDetails.getInitiation().getFinalPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(FINAL_PAYMENT_AMOUNT).get(AMOUNT).getAsString());
        assertThat(responseDetails.getInitiation().getFinalPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(FINAL_PAYMENT_AMOUNT).get(CURRENCY).getAsString());

        assertThat(responseDetails.getInitiation().getFirstPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(FIRST_PAYMENT_AMOUNT).get(AMOUNT).getAsString());
        assertThat(responseDetails.getInitiation().getFirstPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(FIRST_PAYMENT_AMOUNT).get(CURRENCY).getAsString());

        assertThat(responseDetails.getInitiation().getRecurringPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(RECURRING_PAYMENT_AMOUNT).get(AMOUNT).getAsString());
        assertThat(responseDetails.getInitiation().getRecurringPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(RECURRING_PAYMENT_AMOUNT).get(CURRENCY).getAsString());
    }
}
