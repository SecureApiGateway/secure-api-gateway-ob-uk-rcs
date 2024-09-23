/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.BaseCreateInternationalPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.BasePaymentConsentWithExchangeRateInformation;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.joda.time.DateTime;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentConsentWithExchangeRateInformationValidationHelpers {

    public static void validateCreateConsentAgainstCreateRequest(BasePaymentConsentWithExchangeRateInformation<?> consent,
                                                                 BaseCreateInternationalPaymentConsentRequest<?> createConsentRequest) {
        validateCreateConsentAgainstCreateRequest(consent, createConsentRequest, OBVersion.v3_1_10);
    }

    public static void validateCreateConsentAgainstCreateRequest(BasePaymentConsentWithExchangeRateInformation<?> consent,
                                                                 BaseCreateInternationalPaymentConsentRequest<?> createConsentRequest,
                                                                 OBVersion expectedVersion) {
        assertThat(consent.getId()).isNotEmpty();
        assertThat(consent.getStatus()).isEqualTo(OBPaymentConsentStatus.AWAITINGAUTHORISATION.toString());
        assertThat(consent.getApiClientId()).isEqualTo(createConsentRequest.getApiClientId());
        assertThat(consent.getRequestObj()).isEqualTo(createConsentRequest.getConsentRequest());
        assertThat(consent.getRequestVersion()).isEqualTo(expectedVersion);
        assertThat(consent.getCharges()).isEqualTo(createConsentRequest.getCharges());
        assertThat(consent.getExchangeRateInformation()).isEqualTo(createConsentRequest.getExchangeRateInformation());
        assertThat(consent.getIdempotencyKey()).isEqualTo(createConsentRequest.getIdempotencyKey());
        assertThat(consent.getResourceOwnerId()).isNull();
        assertThat(consent.getAuthorisedDebtorAccountId()).isNull();

        assertThat(consent.getIdempotencyKeyExpiration()).isGreaterThan(DateTime.now());
        assertThat(consent.getCreationDateTime()).isBefore(new Date());
        assertThat(consent.getStatusUpdateDateTime()).isEqualTo(consent.getCreationDateTime());
    }

}
