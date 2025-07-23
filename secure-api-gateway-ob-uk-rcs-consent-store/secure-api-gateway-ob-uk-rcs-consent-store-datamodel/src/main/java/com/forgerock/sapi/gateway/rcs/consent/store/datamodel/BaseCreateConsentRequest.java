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
package com.forgerock.sapi.gateway.rcs.consent.store.datamodel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

@Validated
public abstract class BaseCreateConsentRequest<T> {

    @NotNull
    private String apiClientId;

    private String consentId;

    @NotNull
    @Valid
    private T consentRequest;

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getApiClientId() {
        return apiClientId;
    }

    public void setApiClientId(String apiClientId) {
        this.apiClientId = apiClientId;
    }

    public T getConsentRequest() {
        return consentRequest;
    }

    public void setConsentRequest(T consentRequest) {
        this.consentRequest = consentRequest;
    }

    @Override
    public String toString() {
        return "BaseCreateConsentRequest{" +
                "apiClientId='" + apiClientId + '\'' +
                ", consentRequest=" + consentRequest +
                '}';
    }
}
