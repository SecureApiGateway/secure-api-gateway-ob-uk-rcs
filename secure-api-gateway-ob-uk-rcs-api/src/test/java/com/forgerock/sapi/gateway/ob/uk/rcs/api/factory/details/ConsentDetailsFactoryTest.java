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
package com.forgerock.sapi.gateway.ob.uk.rcs.api.factory.details;

import com.forgerock.sapi.gateway.ob.uk.rcs.api.factory.details.decoder.FRAccountIdentifierDecoder;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.IntentType;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetailsConstants;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticPaymentConsentDetailsTestFactory.aValidDomesticPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticScheduledPaymentConsentDetailsTestFactory.aValidDomesticScheduledPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticStandingOrderConsentDetailsTestFactory.aValidDomesticStandingOrderConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory.aValidDomesticVrpPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.FilePaymentConsentDetailsTestFactory.aValidFilePaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.InternationalPaymentConsentDetailsTestFactory.aValidInternationalPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.InternationalScheduledPaymentConsentDetailsTestFactory.aValidInternationalScheduledPaymentConsentDetails;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.test.support.InternationalStandingOrderConsentDetailsTestFactory.aValidInternationalStandingOrderConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Unit test for {@link ConsentDetailsFactory} factories
 */
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ConsentDetailsFactoryProvider.class,
        AccountConsentDetailsFactory.class,
        DomesticPaymentConsentDetailsFactory.class,
        DomesticScheduledPaymentConsentDetailsFactory.class,
        DomesticStandingOrderConsentDetailsFactory.class,
        DomesticVrpPaymentConsentDetailsFactory.class,
        InternationalPaymentConsentDetailsFactory.class,
        InternationalScheduledPaymentConsentDetailsFactory.class,
        InternationalStandingOrderConsentDetailsFactory.class,
        FilePaymentConsentDetailsFactory.class,
        FRAccountIdentifierDecoder.class
})
public class ConsentDetailsFactoryTest {

    public static final String ACCOUNT_INTENT_ID = "AAC_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    public static final String DOMESTIC_PAYMENT_INTENT_ID = "PDC_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    public static final String DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID = "PDSC_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    public static final String DOMESTIC_STANDING_ORDER_INTENT_ID = "PDSOC_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    public static final String INTERNATIONAL_PAYMENT_INTENT_ID = "PIC_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    public static final String INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID = "PISC_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    public static final String INTERNATIONAL_STANDING_ORDER_INTENT_ID = "PISOC_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    public static final String FILE_PAYMENT_INTENT_ID = "PFC_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    public static final String DOMESTIC_VRP_PAYMENT_INTENT_ID = "DVRP_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    @Autowired
    private ConsentDetailsFactoryProvider consentDetailsFactoryProvider;

    private static Stream<Arguments> validArguments() {
        return Stream.of(
                arguments(
                        IntentType.ACCOUNT_ACCESS_CONSENT,
                        ACCOUNT_INTENT_ID,
                        aValidAccountConsentDetails(ACCOUNT_INTENT_ID),
                        AccountConsentDetailsFactory.class
                ),
                arguments(
                        IntentType.PAYMENT_DOMESTIC_CONSENT,
                        DOMESTIC_PAYMENT_INTENT_ID,
                        aValidDomesticPaymentConsentDetails(DOMESTIC_PAYMENT_INTENT_ID),
                        DomesticPaymentConsentDetailsFactory.class
                ),
                arguments(
                        IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT,
                        DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID,
                        aValidDomesticScheduledPaymentConsentDetails(DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID),
                        DomesticScheduledPaymentConsentDetailsFactory.class
                ),
                arguments(
                        IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT,
                        DOMESTIC_STANDING_ORDER_INTENT_ID,
                        aValidDomesticStandingOrderConsentDetails(DOMESTIC_STANDING_ORDER_INTENT_ID),
                        DomesticStandingOrderConsentDetailsFactory.class
                ),
                arguments(
                        IntentType.DOMESTIC_VRP_PAYMENT_CONSENT,
                        DOMESTIC_VRP_PAYMENT_INTENT_ID,
                        aValidDomesticVrpPaymentConsentDetails(DOMESTIC_VRP_PAYMENT_INTENT_ID),
                        DomesticVrpPaymentConsentDetailsFactory.class
                ),
                arguments(
                        IntentType.PAYMENT_INTERNATIONAL_CONSENT,
                        INTERNATIONAL_PAYMENT_INTENT_ID,
                        aValidInternationalPaymentConsentDetails(INTERNATIONAL_PAYMENT_INTENT_ID),
                        InternationalPaymentConsentDetailsFactory.class
                ),
                arguments(
                        IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT,
                        INTERNATIONAL_STANDING_ORDER_INTENT_ID,
                        aValidInternationalStandingOrderConsentDetails(INTERNATIONAL_STANDING_ORDER_INTENT_ID),
                        InternationalStandingOrderConsentDetailsFactory.class
                ),
                arguments(
                        IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT,
                        INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID,
                        aValidInternationalScheduledPaymentConsentDetails(INTERNATIONAL_SCHEDULED_PAYMENT_INTENT_ID),
                        InternationalScheduledPaymentConsentDetailsFactory.class
                ),
                arguments(
                        IntentType.PAYMENT_FILE_CONSENT,
                        FILE_PAYMENT_INTENT_ID,
                        aValidFilePaymentConsentDetails(FILE_PAYMENT_INTENT_ID),
                        FilePaymentConsentDetailsFactory.class
                )
        );
    }

    @ParameterizedTest
    @MethodSource("validArguments")
    public void shouldDecodeJson(IntentType intentType, String intentId, JsonObject jsonObject, Class expectedClass) {
        ConsentDetailsFactory factory = consentDetailsFactoryProvider.getFactory(intentType);
        assertThat(factory).isExactlyInstanceOf(expectedClass);
        assertThat(factory.getIntentType()).isEqualTo(intentType);
        ConsentDetails details = factory.decode(jsonObject);
        details.setConsentId(intentId);
        JsonObject obIntentObject = jsonObject.get(ConsentDetailsConstants.Intent.OB_INTENT_OBJECT).getAsJsonObject();
        assertThat(details.getIntentType()).isEqualTo(intentType);
        assertThat(details.getConsentId()).isEqualTo(
                obIntentObject.get(
                                ConsentDetailsConstants.Intent.Members.DATA).getAsJsonObject()
                        .get(ConsentDetailsConstants.Intent.CONSENT_ID).getAsString()
        );
    }
}
