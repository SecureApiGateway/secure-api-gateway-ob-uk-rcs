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

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.springframework.validation.annotation.Validated;

import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.BaseConsent;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5DataCharges;

@Validated
public class DomesticPaymentConsent extends BaseConsent<OBWriteDomesticConsent4> {

    @NotNull
    private String idempotencyKey;

    @NotNull
    private DateTime idempotencyKeyExpiration;

    private String authorisedDebtorAccountId;

    @Valid
    private List<OBWriteDomesticConsentResponse5DataCharges> charges;

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

    public List<OBWriteDomesticConsentResponse5DataCharges> getCharges() {
        return charges;
    }

    public void setCharges(List<OBWriteDomesticConsentResponse5DataCharges> charges) {
        this.charges = charges;
    }
}
