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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.joda.time.DateTime;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.BaseCreatePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.BasePaymentConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;

public class PaymentConsentValidationHelpers {

    public static void validateCreateConsentAgainstCreateRequest(BasePaymentConsent<?> consent,
            BaseCreatePaymentConsentRequest<?> createConsentRequest) {
        validateCreateConsentAgainstCreateRequest(consent, createConsentRequest, OBVersion.v3_1_10);
    }

    public static void validateCreateConsentAgainstCreateRequestV4(BasePaymentConsent<?> consent,
                                                                 BaseCreatePaymentConsentRequest<?> createConsentRequest) {
        validateCreateConsentAgainstCreateRequest(consent, createConsentRequest, OBVersion.v4_0_0);
    }

    public static void validateCreateConsentAgainstCreateRequest(BasePaymentConsent<?> consent,
                                                                 BaseCreatePaymentConsentRequest<?> createConsentRequest,
                                                                 OBVersion expectedVersion) {
        assertThat(consent.getId()).isNotEmpty();
        assertThat(consent.getStatus()).isEqualTo(OBPaymentConsentStatus.AWAITINGAUTHORISATION.toString());
        assertThat(consent.getApiClientId()).isEqualTo(createConsentRequest.getApiClientId());
        assertThat(consent.getRequestObj()).isEqualTo(createConsentRequest.getConsentRequest());
        assertThat(consent.getRequestVersion()).isEqualTo(expectedVersion);
        assertThat(consent.getCharges()).isEqualTo(createConsentRequest.getCharges());
        assertThat(consent.getIdempotencyKey()).isEqualTo(createConsentRequest.getIdempotencyKey());
        assertThat(consent.getResourceOwnerId()).isNull();
        assertThat(consent.getAuthorisedDebtorAccountId()).isNull();

        assertThat(consent.getIdempotencyKeyExpiration()).isGreaterThan(DateTime.now());
        assertThat(consent.getCreationDateTime()).isBefore(new Date());
        assertThat(consent.getStatusUpdateDateTime()).isEqualTo(consent.getCreationDateTime());
    }

    public static void validateAuthorisedConsent(BasePaymentConsent<?> authorisedConsent, AuthorisePaymentConsentRequest authoriseReq, BasePaymentConsent<?> originalConsent) {
        assertThat(authorisedConsent.getStatus()).isEqualTo(OBPaymentConsentStatus.AUTHORISED.toString());
        assertThat(authorisedConsent.getResourceOwnerId()).isEqualTo(authoriseReq.getResourceOwnerId());
        assertThat(authorisedConsent.getAuthorisedDebtorAccountId()).isEqualTo(authoriseReq.getAuthorisedDebtorAccountId());
        validateUpdatedConsentAgainstOriginal(authorisedConsent, originalConsent);
    }

    public static void validateRejectedConsent(BasePaymentConsent<?> rejectedConsent, RejectConsentRequest rejectReq, BasePaymentConsent<?> originalConsent) {
        assertThat(rejectedConsent.getStatus()).isEqualTo(OBPaymentConsentStatus.REJECTED.toString());
        assertThat(rejectedConsent.getResourceOwnerId()).isEqualTo(rejectReq.getResourceOwnerId());
        validateUpdatedConsentAgainstOriginal(rejectedConsent, originalConsent);
    }

    /**
     * Validates fields in an updatedConsent vs an original consent.
     *
     * This checks that fields that should never change when a consent is updated do never change, and verifies that
     * the statusUpdateDateTime increases vs the original.
     */
    public static void validateUpdatedConsentAgainstOriginal(BasePaymentConsent<?> updatedConsent, BasePaymentConsent<?> consent) {
        assertThat(updatedConsent.getId()).isEqualTo(consent.getId());
        assertThat(updatedConsent.getApiClientId()).isEqualTo(consent.getApiClientId());
        assertThat(updatedConsent.getRequestObj()).isEqualTo(consent.getRequestObj());
        assertThat(updatedConsent.getRequestVersion()).isEqualTo(consent.getRequestVersion());
        assertThat(updatedConsent.getCreationDateTime()).isEqualTo(consent.getCreationDateTime());
        assertThat(updatedConsent.getStatusUpdateDateTime()).isBeforeOrEqualTo(new Date()).isAfterOrEqualTo(consent.getStatusUpdateDateTime());
    }

    /**
     * Validates fields of a consumedConsent against an authorisedConsent (the previous state)
     *
     * Checks that data added to the consent during authorisation is present in the consumedConsent
     */
    public static void validateConsumedConsent(BasePaymentConsent<?> consumedConsent, BasePaymentConsent<?> authorisedConsent) {
        assertThat(consumedConsent.getStatus()).isEqualTo(OBPaymentConsentStatus.CONSUMED.toString());
        validateUpdatedConsentAgainstOriginal(consumedConsent, authorisedConsent);

        assertThat(consumedConsent.getStatusUpdateDateTime()).isAfterOrEqualTo(authorisedConsent.getStatusUpdateDateTime());
        assertThat(consumedConsent.getAuthorisedDebtorAccountId()).isEqualTo(authorisedConsent.getAuthorisedDebtorAccountId());
        assertThat(consumedConsent.getResourceOwnerId()).isEqualTo(authorisedConsent.getResourceOwnerId());
    }
}
