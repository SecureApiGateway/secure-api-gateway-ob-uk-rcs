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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domesticscheduled.v3_1_10;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.CreateDomesticScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;

@Validated
@Api(tags = {"v3.1.10"})
@RequestMapping(value = "/consent/store/v3.1.10")
public interface DomesticScheduledPaymentConsentApi {

    @ApiOperation(value = "Create Domestic Scheduled Payment Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "DomesticScheduledPaymentConsent object representing the consent created",
                         response = DomesticScheduledPaymentConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/domestic-scheduled-payment-consents",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<DomesticScheduledPaymentConsent> createConsent(@ApiParam(value = "Create Consent Request", required = true)
                                                                  @Valid
                                                                  @RequestBody CreateDomesticScheduledPaymentConsentRequest request);


    @ApiOperation(value = "Get Domestic Scheduled Payment Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "DomesticScheduledPaymentConsent object representing the consent created",
                         response = DomesticScheduledPaymentConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/domestic-scheduled-payment-consents/{consentId}",
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.GET)
    ResponseEntity<DomesticScheduledPaymentConsent> getConsent(@PathVariable(value = "consentId") String consentId,
                                                               @RequestHeader(value = "x-api-client-id") String apiClientId);


    @ApiOperation(value = "Authorise Domestic Scheduled Payment Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "DomesticScheduledPaymentConsent object representing the consent created",
                         response = DomesticScheduledPaymentConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/domestic-scheduled-payment-consents/{consentId}/authorise",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<DomesticScheduledPaymentConsent> authoriseConsent(@PathVariable(value = "consentId") String consentId,
                                                                     @ApiParam(value = "Authorise Consent Request", required = true)
                                                                     @Valid
                                                                     @RequestBody AuthorisePaymentConsentRequest request);


    @ApiOperation(value = "Reject Domestic Scheduled Payment Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "DomesticScheduledPaymentConsent object representing the consent created",
                         response = DomesticScheduledPaymentConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/domestic-scheduled-payment-consents/{consentId}/reject",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<DomesticScheduledPaymentConsent> rejectConsent(@PathVariable(value = "consentId") String consentId,
                                                                  @ApiParam(value = "Reject Consent Request", required = true)
                                                                  @Valid
                                                                  @RequestBody RejectConsentRequest request);



    @ApiOperation(value = "Consume Domestic Scheduled Payment Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "DomesticScheduledPaymentConsent object representing the consent created",
                         response = DomesticScheduledPaymentConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/domestic-scheduled-payment-consents/{consentId}/consume",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<DomesticScheduledPaymentConsent> consumeConsent(@PathVariable(value = "consentId") String consentId,
                                                                   @ApiParam(value = "Consume Consent Request", required = true)
                                                                   @Valid
                                                                   @RequestBody ConsumePaymentConsentRequest request);

}
