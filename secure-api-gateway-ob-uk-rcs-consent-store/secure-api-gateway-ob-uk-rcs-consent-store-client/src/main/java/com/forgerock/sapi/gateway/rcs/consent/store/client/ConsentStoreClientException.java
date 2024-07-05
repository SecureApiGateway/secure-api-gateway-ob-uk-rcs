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
package com.forgerock.sapi.gateway.rcs.consent.store.client;


import uk.org.openbanking.datamodel.v3.error.OBError1;

public class ConsentStoreClientException extends RuntimeException {

    public enum ErrorType {
        INVALID_PERMISSIONS,
        NOT_FOUND,
        BAD_REQUEST,
        INVALID_STATE_TRANSITION,
        FAILED_TO_DECODE_RESPONSE,
        IDEMPOTENCY_ERROR,
        UNKNOWN
    }

    private final ErrorType errorType;

    private OBError1 obError1;

    public ConsentStoreClientException(ErrorType errorType, String message) {
        this(errorType, message, null);
    }

    public ConsentStoreClientException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public void setObError1(OBError1 obError1) {
        this.obError1 = obError1;
    }

    public OBError1 getObError1() {
        return obError1;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
