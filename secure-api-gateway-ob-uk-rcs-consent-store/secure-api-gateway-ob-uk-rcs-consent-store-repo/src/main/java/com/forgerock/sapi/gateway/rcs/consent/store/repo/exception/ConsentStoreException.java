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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.exception;

public class ConsentStoreException extends RuntimeException {

    public enum ErrorType {
        INVALID_PERMISSIONS,
        NOT_FOUND,
        BAD_REQUEST,
        INVALID_STATE_TRANSITION,
        INVALID_CONSENT_DECISION,
        INVALID_DEBTOR_ACCOUNT,
        INVALID_API_VERSION,
        CONSENT_REAUTHENTICATION_NOT_SUPPORTED,
        IDEMPOTENCY_ERROR
    }

    private final ErrorType errorType;

    private final String consentId;


    public ConsentStoreException(ErrorType errorType, String consentId) {
        this(errorType, consentId, (Throwable) null);
    }

    public ConsentStoreException(ErrorType errorType, String consentId, Throwable cause) {
        super(errorType.name() + (consentId != null ? " for consentId: " + consentId : ""), cause);
        this.errorType = errorType;
        this.consentId = consentId;
    }

    public ConsentStoreException(ErrorType errorType, String consentId, String message) {
        this(errorType, consentId, message, null);

    }

    public ConsentStoreException(ErrorType errorType, String consentId, String message, Throwable cause) {
        super(errorType.name() + (consentId != null ? " for consentId: " + consentId : "") + ", additional details: " + message, cause);
        this.errorType = errorType;
        this.consentId = consentId;
    }

    public String getConsentId() {
        return consentId;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
