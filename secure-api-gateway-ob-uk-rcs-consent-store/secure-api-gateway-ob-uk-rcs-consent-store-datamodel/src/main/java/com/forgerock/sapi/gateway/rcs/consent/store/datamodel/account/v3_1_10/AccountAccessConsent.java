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

import org.springframework.validation.annotation.Validated;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRReadConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.BaseConsent;

/**
 * OBIE Account Access Consent: https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/account-access-consents.html
 */
@Validated
public class AccountAccessConsent extends BaseConsent<FRReadConsent> {

    private List<String> authorisedAccountIds;

    public void setAuthorisedAccountIds(List<String> authorisedAccountIds) {
        this.authorisedAccountIds = authorisedAccountIds;
    }

    public List<String> getAuthorisedAccountIds() {
        return authorisedAccountIds;
    }

    @Override
    public String toString() {
        return "AccountAccessConsent{" +
                "authorisedAccountIds=" + authorisedAccountIds +
                ", id='" + getId() + '\'' +
                ", requestObj=" + getRequestObj() +
                ", requestVersion=" + getRequestVersion() +
                ", status='" + getStatus() + '\'' +
                ", apiClientId='" + getApiClientId() + '\'' +
                ", resourceOwnerId='" + getResourceOwnerId() + '\'' +
                ", creationDateTime=" + getCreationDateTime() +
                ", statusUpdateDateTime=" + getStatusUpdateDateTime() +
                '}';
    }
}
