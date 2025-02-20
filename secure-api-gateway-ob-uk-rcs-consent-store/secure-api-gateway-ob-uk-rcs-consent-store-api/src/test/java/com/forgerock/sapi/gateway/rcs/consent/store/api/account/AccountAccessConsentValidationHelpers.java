/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rcs.consent.store.api.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AuthoriseAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.v3.common.OBExternalRequestStatus1Code;

/**
 * Helper methods for validating {@link AccountAccessConsent} objects as part of using the {@link AccountAccessConsentApi}
 */
public class AccountAccessConsentValidationHelpers {

    /**
     * Validates that a created {@link AccountAccessConsent} against the original {@link CreateAccountAccessConsentRequest}
     */
    public static void validateCreateConsentAgainstCreateRequest(AccountAccessConsent consent,
                                                                 CreateAccountAccessConsentRequest createAccountAccessConsentRequest, OBVersion obVersion) {
        assertThat(consent.getId()).isNotEmpty();
        assertThat(consent.getStatus()).isEqualTo(OBExternalRequestStatus1Code.AWAITINGAUTHORISATION.toString());
        assertThat(consent.getApiClientId()).isEqualTo(createAccountAccessConsentRequest.getApiClientId());
        assertThat(consent.getRequestObj()).isEqualTo(createAccountAccessConsentRequest.getConsentRequest());
        assertThat(consent.getRequestVersion()).isEqualTo(obVersion);
        assertThat(consent.getResourceOwnerId()).isNull();
        assertThat(consent.getAuthorisedAccountIds()).isNull();

        assertThat(consent.getCreationDateTime()).isBefore(new Date());
        assertThat(consent.getStatusUpdateDateTime()).isEqualTo(consent.getCreationDateTime());
    }

    public static void validateAuthorisedConsent(AccountAccessConsent authorisedConsent, AuthoriseAccountAccessConsentRequest authoriseReq, AccountAccessConsent originalConsent) {
        assertThat(authorisedConsent.getStatus()).isEqualTo(OBExternalRequestStatus1Code.AUTHORISED.toString());
        assertThat(authorisedConsent.getResourceOwnerId()).isEqualTo(authoriseReq.getResourceOwnerId());
        assertThat(authorisedConsent.getAuthorisedAccountIds()).isEqualTo(authoriseReq.getAuthorisedAccountIds());
        validateUpdatedConsentAgainstOriginal(authorisedConsent, originalConsent);
    }

    public static void validateRejectedConsent(AccountAccessConsent rejectedConsent, RejectConsentRequest rejectReq, AccountAccessConsent originalConsent) {
        assertThat(rejectedConsent.getStatus()).isEqualTo(OBExternalRequestStatus1Code.REJECTED.toString());
        assertThat(rejectedConsent.getResourceOwnerId()).isEqualTo(rejectReq.getResourceOwnerId());
        validateUpdatedConsentAgainstOriginal(rejectedConsent, originalConsent);
    }

    /**
     * Validates fields in an updatedConsent vs an original consent.
     *
     * This checks that fields that should never change when a consent is updated do never change, and verifies that
     * the statusUpdateDateTime increases vs the original.
     */
    public static void validateUpdatedConsentAgainstOriginal(AccountAccessConsent updatedConsent, AccountAccessConsent consent) {
        assertThat(updatedConsent.getId()).isEqualTo(consent.getId());
        assertThat(updatedConsent.getApiClientId()).isEqualTo(consent.getApiClientId());
        assertThat(updatedConsent.getRequestObj()).isEqualTo(consent.getRequestObj());
        assertThat(updatedConsent.getRequestVersion()).isEqualTo(consent.getRequestVersion());
        assertThat(updatedConsent.getCreationDateTime()).isEqualTo(consent.getCreationDateTime());
        assertThat(updatedConsent.getStatusUpdateDateTime()).isBeforeOrEqualTo(new Date()).isAfterOrEqualTo(consent.getStatusUpdateDateTime());
    }
}
