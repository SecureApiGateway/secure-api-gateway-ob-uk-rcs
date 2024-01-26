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
package com.forgerock.sapi.gateway.rcs.consent.store.api.funds.v3_1_10;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.AuthoriseFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.CreateFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsentResponse1Data.StatusEnum;

/**
 * Helper methods for validating {@link FundsConfirmationConsent} objects as part of using the {@link FundsConfirmationConsentApi}
 */
public class FundsConfirmationConsentValidationHelpers {

    public static void validateCreateConsentAgainstCreateRequest(FundsConfirmationConsent consent,
                                                                 CreateFundsConfirmationConsentRequest createFundsConfirmationConsentRequest) {
        assertThat(consent.getId()).isNotEmpty();
        assertThat(consent.getStatus()).isEqualTo(StatusEnum.AWAITINGAUTHORISATION.toString());
        assertThat(consent.getApiClientId()).isEqualTo(createFundsConfirmationConsentRequest.getApiClientId());
        assertThat(consent.getRequestObj()).isEqualTo(createFundsConfirmationConsentRequest.getConsentRequest());
        assertThat(consent.getRequestVersion()).isEqualTo(OBVersion.v3_1_10);
        assertThat(consent.getResourceOwnerId()).isNull();
        assertThat(consent.getCreationDateTime()).isBefore(new Date());
        assertThat(consent.getStatusUpdateDateTime()).isEqualTo(consent.getCreationDateTime());
    }

    public static void validateAuthorisedConsent(FundsConfirmationConsent authorisedConsent, AuthoriseFundsConfirmationConsentRequest authoriseReq, FundsConfirmationConsent originalConsent) {
        assertThat(authorisedConsent.getStatus()).isEqualTo(StatusEnum.AUTHORISED.toString());
        assertThat(authorisedConsent.getResourceOwnerId()).isEqualTo(authoriseReq.getResourceOwnerId());
        assertThat(authorisedConsent.getAuthorisedDebtorAccountId()).isEqualTo(authoriseReq.getAuthorisedDebtorAccountId());
        validateUpdatedConsentAgainstOriginal(authorisedConsent, originalConsent);
    }

    public static void validateRejectedConsent(FundsConfirmationConsent rejectedConsent, RejectConsentRequest rejectReq, FundsConfirmationConsent originalConsent) {
        assertThat(rejectedConsent.getStatus()).isEqualTo(StatusEnum.REJECTED.toString());
        assertThat(rejectedConsent.getResourceOwnerId()).isEqualTo(rejectReq.getResourceOwnerId());
        validateUpdatedConsentAgainstOriginal(rejectedConsent, originalConsent);
    }

    /**
     * Validates fields in an updatedConsent vs an original consent.
     * <p>
     * This checks that fields that should never change when a consent is updated do never change, and verifies that
     * the statusUpdateDateTime increases vs the original.
     */
    public static void validateUpdatedConsentAgainstOriginal(FundsConfirmationConsent updatedConsent, FundsConfirmationConsent consent) {
        assertThat(updatedConsent.getId()).isEqualTo(consent.getId());
        assertThat(updatedConsent.getApiClientId()).isEqualTo(consent.getApiClientId());
        assertThat(updatedConsent.getRequestObj()).isEqualTo(consent.getRequestObj());
        assertThat(updatedConsent.getRequestVersion()).isEqualTo(consent.getRequestVersion());
        assertThat(updatedConsent.getCreationDateTime()).isEqualTo(consent.getCreationDateTime());
        assertThat(updatedConsent.getStatusUpdateDateTime()).isBeforeOrEqualTo(new Date()).isAfterOrEqualTo(consent.getStatusUpdateDateTime());
    }
}
