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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment;

import java.util.List;

import org.joda.time.DateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.BaseConsentEntity;

/**
 * Base class containing fields common to all Payment Consent Types
 */
public abstract class BasePaymentConsentEntity<T> extends BaseConsentEntity<T> {
    /**
     * Key supplied by the ApiClient when creating the Consent, to enable the request to be made idempotent
     */
    @NotNull
    private String idempotencyKey;
    /**
     * Time at which the use of the idempotencyKey expires, and the ApiClient is then able to reuse it with a different
     * Consent Request
     */
    @NotNull
    private DateTime idempotencyKeyExpiration;
    /**
     * Id of the DebtorAccount that the Resource Owner has authorised that the payment can be taken from
     *
     * This field is set as part of Consent Authorisation, therefore may be null in other states.
     */
    private String authorisedDebtorAccountId;
    /**
     * Optional - charges applied to the payment transaction
     */
    @Valid
    private List<FRCharge> charges;

    public void setAuthorisedDebtorAccountId(String authorisedDebtorAccountId) {
        this.authorisedDebtorAccountId = authorisedDebtorAccountId;
    }

    public String getAuthorisedDebtorAccountId() {
        return authorisedDebtorAccountId;
    }

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

    public List<FRCharge> getCharges() {
        return charges;
    }

    public void setCharges(List<FRCharge> charges) {
        this.charges = charges;
    }
}
