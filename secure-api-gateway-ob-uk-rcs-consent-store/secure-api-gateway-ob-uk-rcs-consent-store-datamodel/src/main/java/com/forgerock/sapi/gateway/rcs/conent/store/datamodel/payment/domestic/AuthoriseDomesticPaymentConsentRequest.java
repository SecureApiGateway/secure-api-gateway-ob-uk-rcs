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
package com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

@Validated
public class AuthoriseDomesticPaymentConsentRequest {

    @NotNull
    private String apiClientId;

    @NotNull
    private String consentId;

    @NotNull
    private String resourceOwnerId;

    @NotNull
    private String authorisedDebtorAccountId;

    public AuthoriseDomesticPaymentConsentRequest() {
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getResourceOwnerId() {
        return resourceOwnerId;
    }

    public void setResourceOwnerId(String resourceOwnerId) {
        this.resourceOwnerId = resourceOwnerId;
    }

    public String getAuthorisedDebtorAccountId() {
        return authorisedDebtorAccountId;
    }

    public void setAuthorisedDebtorAccountId(String authorisedDebtorAccountId) {
        this.authorisedDebtorAccountId = authorisedDebtorAccountId;
    }

    public String getApiClientId() {
        return apiClientId;
    }

    public void setApiClientId(String apiClientId) {
        this.apiClientId = apiClientId;
    }
}
