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
package com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.springframework.validation.annotation.Validated;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.BaseConsent;

@Validated
public abstract class BasePaymentConsent<T> extends BaseConsent<T> {

    @NotNull
    private String idempotencyKey;
    @NotNull
    private DateTime idempotencyKeyExpiration;
    private String authorisedDebtorAccountId;
    @Valid
    private List<FRCharge> charges;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public DateTime getIdempotencyKeyExpiration() {
        return idempotencyKeyExpiration;
    }

    public void setIdempotencyKeyExpiration(DateTime idempotencyKeyExpiration) {
        this.idempotencyKeyExpiration = idempotencyKeyExpiration;
    }

    public String getAuthorisedDebtorAccountId() {
        return authorisedDebtorAccountId;
    }

    public void setAuthorisedDebtorAccountId(String authorisedDebtorAccountId) {
        this.authorisedDebtorAccountId = authorisedDebtorAccountId;
    }

    public List<FRCharge> getCharges() {
        return charges;
    }

    public void setCharges(List<FRCharge> charges) {
        this.charges = charges;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "idempotencyKey='" + idempotencyKey + '\'' +
                ", idempotencyKeyExpiration=" + idempotencyKeyExpiration +
                ", authorisedDebtorAccountId='" + authorisedDebtorAccountId + '\'' +
                ", charges=" + charges +
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
