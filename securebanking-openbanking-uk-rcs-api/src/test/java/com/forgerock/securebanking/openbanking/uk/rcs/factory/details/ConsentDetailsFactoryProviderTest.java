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
package com.forgerock.securebanking.openbanking.uk.rcs.factory.details;

import com.forgerock.securebanking.platform.client.IntentType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Unit test for {@link ConsentDetailsFactoryProvider}
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
        FilePaymentConsentDetailsFactory.class,
        FundsConfirmationConsentDetailsFactory.class,
        InternationalPaymentConsentDetailsFactory.class,
        InternationalScheduledPaymentConsentDetailsFactory.class,
        InternationalStandingOrderConsentDetailsFactory.class
})
public class ConsentDetailsFactoryProviderTest {
    @Autowired
    private ConsentDetailsFactoryProvider consentDetailsFactoryProvider;

    private static Stream<Arguments> validArguments() {
        return Stream.of(
                arguments(IntentType.ACCOUNT_ACCESS_CONSENT, AccountConsentDetailsFactory.class),
                arguments(IntentType.PAYMENT_DOMESTIC_CONSENT, DomesticPaymentConsentDetailsFactory.class),
                arguments(IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT, DomesticScheduledPaymentConsentDetailsFactory.class),
                arguments(IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT, DomesticStandingOrderConsentDetailsFactory.class),
                arguments(IntentType.DOMESTIC_VRP_PAYMENT_CONSENT, DomesticVrpPaymentConsentDetailsFactory.class),
                arguments(IntentType.PAYMENT_FILE_CONSENT, FilePaymentConsentDetailsFactory.class),
                arguments(IntentType.FUNDS_CONFIRMATION_CONSENT, FundsConfirmationConsentDetailsFactory.class),
                arguments(IntentType.PAYMENT_INTERNATIONAL_CONSENT, InternationalPaymentConsentDetailsFactory.class),
                arguments(IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT, InternationalStandingOrderConsentDetailsFactory.class),
                arguments(IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT, InternationalScheduledPaymentConsentDetailsFactory.class)
        );
    }

    @ParameterizedTest
    @MethodSource("validArguments")
    public void shouldHaveFactory(IntentType intentType, Class expectedClass) {
        ConsentDetailsFactory factory = consentDetailsFactoryProvider.getFactory(intentType);
        assertThat(factory).isExactlyInstanceOf(expectedClass);
        assertThat(factory.getIntentType()).isEqualTo(intentType);
    }
}
