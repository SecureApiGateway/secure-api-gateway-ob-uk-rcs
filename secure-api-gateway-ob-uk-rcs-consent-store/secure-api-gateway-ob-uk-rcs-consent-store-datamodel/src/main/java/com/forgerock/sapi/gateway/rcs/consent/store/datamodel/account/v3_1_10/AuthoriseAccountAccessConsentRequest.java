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
package com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.BaseAuthoriseConsentRequest;

@Validated
public class AuthoriseAccountAccessConsentRequest extends BaseAuthoriseConsentRequest {

    @NotNull
    @NotEmpty
    private List<String> authorisedAccountIds;

    public List<String> getAuthorisedAccountIds() {
        return authorisedAccountIds;
    }

    public void setAuthorisedAccountIds(List<String> authorisedAccountIds) {
        this.authorisedAccountIds = authorisedAccountIds;
    }

    @Override
    public String toString() {
        return "AuthoriseAccountAccessConsentRequest{" +
                "authorisedAccountIds=" + authorisedAccountIds +
                ", consentId='" + getConsentId() + '\'' +
                ", resourceOwnerId='" + getResourceOwnerId() + '\'' +
                ", apiClientId='" + getApiClientId() + '\'' +
                '}';
    }
}
