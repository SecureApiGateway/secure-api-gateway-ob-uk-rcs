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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetailsConstants;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticScheduledPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticStandingOrderConsentDetails;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.forgerock.FRFrequency;
import com.google.gson.JsonObject;
import org.joda.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsentDetailsTestValidations {

    protected static void validateDomesticScheduledConsentDetailsResponse(
            JsonObject consentDetails,
            DomesticScheduledPaymentConsentDetails responseDetails
    ) {
        assertThat(responseDetails.getPaymentDate()
                .isEqual(
                        Instant.parse(
                                consentDetails.getAsJsonObject(ConsentDetailsConstants.Intent.OB_INTENT_OBJECT)
                                        .getAsJsonObject(ConsentDetailsConstants.Intent.Members.DATA)
                                        .getAsJsonObject(ConsentDetailsConstants.Intent.Members.INITIATION)
                                        .get(ConsentDetailsConstants.Intent.Members.REQUESTED_EXECUTION_DATETIME).getAsString()
                        ).toDateTime()
                )
        );
    }

    protected static void validateDomesticStandingOrderConsentDetailsResponse(
            JsonObject consentDetails,
            DomesticStandingOrderConsentDetails responseDetails
    ) {
        final JsonObject expectedIntentData = consentDetails.getAsJsonObject(ConsentDetailsConstants.Intent.OB_INTENT_OBJECT).getAsJsonObject(ConsentDetailsConstants.Intent.Members.DATA);
        final JsonObject initiation = expectedIntentData.getAsJsonObject(ConsentDetailsConstants.Intent.Members.INITIATION);

        assertThat(responseDetails.getInitiation().getFinalPaymentDateTime()
                .isEqual(Instant.parse(initiation.get(ConsentDetailsConstants.Intent.Members.FINAL_PAYMENT_DATETIME).getAsString()).toDateTime())
        );
        assertThat(responseDetails.getInitiation().getFirstPaymentDateTime()
                .isEqual(Instant.parse(initiation.get(ConsentDetailsConstants.Intent.Members.FIRST_PAYMENT_DATETIME).getAsString()).toDateTime())
        );

        assertThat(responseDetails.getInitiation().getRecurringPaymentDateTime()
                .isEqual(Instant.parse(initiation.get(ConsentDetailsConstants.Intent.Members.RECURRING_PAYMENT_DATETIME).getAsString()).toDateTime())
        );

        assertThat(responseDetails.getInitiation().getFrequency())
                .isEqualTo((new FRFrequency(initiation.get(ConsentDetailsConstants.Intent.Members.FREQUENCY).getAsString())).getSentence());

        assertThat(responseDetails.getInitiation().getFinalPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(ConsentDetailsConstants.Intent.Members.FINAL_PAYMENT_AMOUNT).get(ConsentDetailsConstants.Intent.Members.AMOUNT).getAsString());
        assertThat(responseDetails.getInitiation().getFinalPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(ConsentDetailsConstants.Intent.Members.FINAL_PAYMENT_AMOUNT).get(ConsentDetailsConstants.Intent.Members.CURRENCY).getAsString());

        assertThat(responseDetails.getInitiation().getFirstPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(ConsentDetailsConstants.Intent.Members.FIRST_PAYMENT_AMOUNT).get(ConsentDetailsConstants.Intent.Members.AMOUNT).getAsString());
        assertThat(responseDetails.getInitiation().getFirstPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(ConsentDetailsConstants.Intent.Members.FIRST_PAYMENT_AMOUNT).get(ConsentDetailsConstants.Intent.Members.CURRENCY).getAsString());

        assertThat(responseDetails.getInitiation().getRecurringPaymentAmount().getAmount())
                .isEqualTo(initiation.getAsJsonObject(ConsentDetailsConstants.Intent.Members.RECURRING_PAYMENT_AMOUNT).get(ConsentDetailsConstants.Intent.Members.AMOUNT).getAsString());
        assertThat(responseDetails.getInitiation().getRecurringPaymentAmount().getCurrency())
                .isEqualTo(initiation.getAsJsonObject(ConsentDetailsConstants.Intent.Members.RECURRING_PAYMENT_AMOUNT).get(ConsentDetailsConstants.Intent.Members.CURRENCY).getAsString());
    }
}
