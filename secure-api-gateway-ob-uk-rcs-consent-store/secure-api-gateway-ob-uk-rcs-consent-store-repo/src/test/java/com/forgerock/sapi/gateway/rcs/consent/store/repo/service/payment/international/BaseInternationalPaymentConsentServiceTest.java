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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.international;

import static org.assertj.core.api.Assertions.assertThat;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRExchangeRateConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.international.BaseInternationalPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.BasePaymentConsentServiceTest;

import uk.org.openbanking.datamodel.payment.OBExchangeRateType2Code;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3DataInitiationExchangeRateInformation;

public abstract class BaseInternationalPaymentConsentServiceTest<T extends BaseInternationalPaymentConsentEntity<?>> extends BasePaymentConsentServiceTest<T> {

    protected static FRExchangeRateInformation getExchangeRateInformation(OBWriteInternational3DataInitiationExchangeRateInformation consentRequestExchangeRateInformation) {
        if (consentRequestExchangeRateInformation.getRateType() == OBExchangeRateType2Code.AGREED) {
            return FRExchangeRateConverter.toFRExchangeRateInformation(consentRequestExchangeRateInformation);
        } else {
            throw new UnsupportedOperationException("Test data is only available for AGREED Exchange Rates at the moment");
        }
    }

    @Override
    protected void validateConsentSpecificFields(T expected, T actual) {
        super.validateConsentSpecificFields(expected, actual);
        assertThat(actual.getExchangeRateInformation()).isEqualTo(expected.getExchangeRateInformation());
    }
}
