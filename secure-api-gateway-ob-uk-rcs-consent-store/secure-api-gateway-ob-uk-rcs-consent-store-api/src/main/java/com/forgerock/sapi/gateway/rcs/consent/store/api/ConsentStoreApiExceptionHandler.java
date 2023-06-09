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
package com.forgerock.sapi.gateway.rcs.consent.store.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;

@ControllerAdvice(basePackageClasses = ConsentStoreApiConfiguration.class)
public class ConsentStoreApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = ConsentStoreException.class)
    public ResponseEntity<Object> handleConsentStoreException(ConsentStoreException ex, WebRequest request) {

        final HttpStatus httpStatus = switch (ex.getErrorType()) {
            case NOT_AUTHORISED -> HttpStatus.UNAUTHORIZED;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };

        final String interactionId = request.getHeader("x-fapi-interaction-id");
        final String errorResponseId = interactionId != null ? interactionId : UUID.randomUUID().toString();

        return ResponseEntity.status(httpStatus).body(
                new OBErrorResponse1()
                        .code("OBRI.Consent.Store.Error")
                        .id(errorResponseId)
                        .message(httpStatus.name())
                        .errors(List.of(new OBError1().errorCode(ex.getErrorType().name()).message(ex.getMessage()))));
    }

    // TODO cribbed from the global handler...
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                HttpHeaders headers,
                                                                HttpStatus status,
                                                                WebRequest request) {

        List<OBError1> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(OBRIErrorType.REQUEST_FIELD_INVALID
                    .toOBError1(error.getDefaultMessage())
                    .path(error.getField())
            );
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(OBRIErrorType.REQUEST_OBJECT_INVALID
                    .toOBError1(error.getDefaultMessage())
                    .path(error.getObjectName())
            );
        }

        return handleOBErrorResponse(new OBErrorResponseException(HttpStatus.BAD_REQUEST,
                                                                  OBRIErrorResponseCategory.ARGUMENT_INVALID,
                                                                  errors), request);
    }

    @ExceptionHandler(value = OBErrorResponseException.class)
    public ResponseEntity<Object> handleOBErrorResponse(OBErrorResponseException ex, WebRequest request) {

        return ResponseEntity.status(ex.getStatus()).body(
                new OBErrorResponse1()
                        .code(ex.getCategory().getId())
                        .id(ex.getId() != null ? ex.getId() : request.getHeader("x-fapi-interaction-id"))
                        .message(ex.getCategory().getDescription())
                        .errors(ex.getErrors()));
    }


}

