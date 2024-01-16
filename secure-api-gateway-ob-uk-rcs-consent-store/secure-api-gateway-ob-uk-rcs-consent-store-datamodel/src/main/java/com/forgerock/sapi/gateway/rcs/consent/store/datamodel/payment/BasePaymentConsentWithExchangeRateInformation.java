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

import org.springframework.validation.annotation.Validated;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation;

@Validated
public abstract class BasePaymentConsentWithExchangeRateInformation<T> extends BasePaymentConsent<T> {

    private FRExchangeRateInformation exchangeRateInformation;

    public FRExchangeRateInformation getExchangeRateInformation() {
        return exchangeRateInformation;
    }

    public void setExchangeRateInformation(FRExchangeRateInformation exchangeRateInformation) {
        this.exchangeRateInformation = exchangeRateInformation;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "exchangeRateInformation=" + exchangeRateInformation +
                ", idempotencyKey='" + getIdempotencyKey() + '\'' +
                ", idempotencyKeyExpiration=" + getIdempotencyKeyExpiration() +
                ", authorisedDebtorAccountId='" + getAuthorisedDebtorAccountId() + '\'' +
                ", charges=" + getCharges() +
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
