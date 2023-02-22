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
package com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions;

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDecisionRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;

/**
 * Generic Client Cloud exception object
 */
public class ExceptionClient extends Exception {

    ErrorClient errorClient;

    public ExceptionClient(ConsentClientDecisionRequest consentDecision) {
        super(ErrorType.INTERNAL_SERVER_ERROR.getDescription());
        this.errorClient = ErrorClient.builder()
                .errorType(ErrorType.INTERNAL_SERVER_ERROR)
                .userId(consentDecision.getResourceOwnerUsername())
                .build();
    }

    public ExceptionClient(ConsentClientDetailsRequest consentRequest) {
        super(ErrorType.INTERNAL_SERVER_ERROR.getDescription());
        this.errorClient = ErrorClient.builder()
                .errorType(ErrorType.INTERNAL_SERVER_ERROR)
                .intentId(consentRequest.getIntentId())
                .userId(consentRequest.getUser() != null ? consentRequest.getUser().getId() : null)
                .clientId(consentRequest.getClientId())
                .build();
    }

    public ExceptionClient(ConsentClientDetailsRequest consentRequest, ErrorType errorType) {
        super(errorType != null ? errorType.getDescription() : ErrorType.INTERNAL_SERVER_ERROR.getDescription());
        this.errorClient = ErrorClient.builder()
                .errorType(errorType != null ? errorType : ErrorType.INTERNAL_SERVER_ERROR)
                .intentId(consentRequest.getIntentId())
                .userId(consentRequest.getUser() != null ? consentRequest.getUser().getId() : null)
                .clientId(consentRequest.getClientId())
                .build();
    }

    public ExceptionClient(ConsentClientDecisionRequest consentDecision, ErrorType errorType) {
        super(errorType != null ? errorType.getDescription() : ErrorType.INTERNAL_SERVER_ERROR.getDescription());
        this.errorClient = ErrorClient.builder()
                .errorType(errorType != null ? errorType : ErrorType.INTERNAL_SERVER_ERROR)
                .userId(consentDecision.getResourceOwnerUsername())
                .build();
    }

    public ExceptionClient(ConsentClientDetailsRequest consentRequest, ErrorType errorType, String message) {
        super(message);
        this.errorClient = ErrorClient.builder()
                .errorType(errorType != null ? errorType : ErrorType.INTERNAL_SERVER_ERROR)
                .intentId(consentRequest.getIntentId())
                .userId(consentRequest.getUser() != null ? consentRequest.getUser().getId() : null)
                .clientId(consentRequest.getClientId())
                .build();
    }

    public ExceptionClient(ConsentClientDecisionRequest consentDecision, ErrorType errorType, String message) {
        super(message);
        this.errorClient = ErrorClient.builder()
                .errorType(errorType != null ? errorType : ErrorType.INTERNAL_SERVER_ERROR)
                .userId(consentDecision.getResourceOwnerUsername())
                .build();
    }

    public ExceptionClient(ConsentClientDetailsRequest consentRequest, ErrorType errorType, String message, Exception exception) {
        super(message, exception);
        this.errorClient = ErrorClient.builder()
                .errorType(errorType != null ? errorType : ErrorType.INTERNAL_SERVER_ERROR)
                .intentId(consentRequest.getIntentId())
                .userId(consentRequest.getUser() != null ? consentRequest.getUser().getId() : null)
                .clientId(consentRequest.getClientId())
                .build();
    }

    public ExceptionClient(ConsentClientDecisionRequest consentDecision, ErrorType errorType, String message, Exception exception) {
        super(message, exception);
        this.errorClient = ErrorClient.builder()
                .errorType(errorType != null ? errorType : ErrorType.INTERNAL_SERVER_ERROR)
                .userId(consentDecision.getResourceOwnerUsername())
                .build();
    }

    public ExceptionClient(ErrorClient errorClient) {
        super(errorClient.getErrorType().getDescription());
        this.errorClient = errorClient;
    }

    public ExceptionClient(ErrorClient errorClient, String message) {
        super(message);
        this.errorClient = errorClient;
    }

    public ExceptionClient(ErrorClient errorClient, Exception exception) {
        super(errorClient.getErrorType().getDescription(), exception);
        this.errorClient = errorClient;
    }

    public ExceptionClient(ErrorClient errorClient, String message, Exception exception) {
        super(message, exception);
        this.errorClient = errorClient;
    }

    public ExceptionClient(Exception exception) {
        super(exception);
    }

    public ErrorClient getErrorClient() {
        return errorClient;
    }
}
