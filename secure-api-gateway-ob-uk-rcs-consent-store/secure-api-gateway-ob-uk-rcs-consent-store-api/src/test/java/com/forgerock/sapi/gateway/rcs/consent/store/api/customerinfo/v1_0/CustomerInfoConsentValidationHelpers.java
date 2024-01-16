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
package com.forgerock.sapi.gateway.rcs.consent.store.api.customerinfo.v1_0;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.AuthoriseCustomerInfoConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.CreateCustomerInfoConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.CustomerInfoConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.joda.time.DateTime;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;

import static org.assertj.core.api.Assertions.assertThat;

;

/**
 * Helper methods for validating {@link CustomerInfoConsent} objects as part of using the {@link CustomerInfoConsentApi}
 */
public class CustomerInfoConsentValidationHelpers {

    public static void validateCreateConsentAgainstCreateRequest(CustomerInfoConsent consent,
                                                                 CreateCustomerInfoConsentRequest createCustomerInfoConsentRequest) {
        assertThat(consent.getId()).isNotEmpty();
        assertThat(consent.getStatus()).isEqualTo(StatusEnum.AWAITINGAUTHORISATION.toString());
        assertThat(consent.getApiClientId()).isEqualTo(createCustomerInfoConsentRequest.getApiClientId());
        assertThat(consent.getRequestObj()).isEqualTo(createCustomerInfoConsentRequest.getConsentRequest());
        assertThat(consent.getRequestVersion()).isEqualTo(OBVersion.v1_0);
        assertThat(consent.getResourceOwnerId()).isNull();
        assertThat(consent.getCreationDateTime()).isLessThan(DateTime.now());
        assertThat(consent.getStatusUpdateDateTime()).isEqualTo(consent.getCreationDateTime());
    }

    public static void validateAuthorisedConsent(CustomerInfoConsent authorisedConsent, AuthoriseCustomerInfoConsentRequest authoriseReq, CustomerInfoConsent originalConsent) {
        assertThat(authorisedConsent.getStatus()).isEqualTo(StatusEnum.AUTHORISED.toString());
        assertThat(authorisedConsent.getResourceOwnerId()).isEqualTo(authoriseReq.getResourceOwnerId());
        validateUpdatedConsentAgainstOriginal(authorisedConsent, originalConsent);
    }

    public static void validateRejectedConsent(CustomerInfoConsent rejectedConsent, RejectConsentRequest rejectReq, CustomerInfoConsent originalConsent) {
        assertThat(rejectedConsent.getStatus()).isEqualTo(StatusEnum.REJECTED.toString());
        assertThat(rejectedConsent.getResourceOwnerId()).isEqualTo(rejectReq.getResourceOwnerId());
        validateUpdatedConsentAgainstOriginal(rejectedConsent, originalConsent);
    }

    /**
     * Validates fields in an updatedConsent vs an original consent.
     *
     * This checks that fields that should never change when a consent is updated do never change, and verifies that
     * the statusUpdateDateTime increases vs the original.
     */
    public static void validateUpdatedConsentAgainstOriginal(CustomerInfoConsent updatedConsent, CustomerInfoConsent consent) {
        assertThat(updatedConsent.getId()).isEqualTo(consent.getId());
        assertThat(updatedConsent.getApiClientId()).isEqualTo(consent.getApiClientId());
        assertThat(updatedConsent.getRequestObj()).isEqualTo(consent.getRequestObj());
        assertThat(updatedConsent.getRequestVersion()).isEqualTo(consent.getRequestVersion());
        assertThat(updatedConsent.getCreationDateTime()).isEqualTo(consent.getCreationDateTime());
        assertThat(updatedConsent.getStatusUpdateDateTime()).isLessThanOrEqualTo(DateTime.now()).isGreaterThan(consent.getStatusUpdateDateTime());
    }
}
