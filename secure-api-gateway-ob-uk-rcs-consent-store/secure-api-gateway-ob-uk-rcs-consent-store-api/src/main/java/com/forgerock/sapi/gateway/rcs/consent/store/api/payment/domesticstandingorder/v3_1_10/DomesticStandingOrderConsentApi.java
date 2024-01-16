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
package com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domesticstandingorder.v3_1_10;

import javax.validation.Valid;

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
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.CreateDomesticStandingOrderConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsent;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;

@Validated
@Api(tags = {"v3.1.10"})
@RequestMapping(value = "/consent/store/v3.1.10")
public interface DomesticStandingOrderConsentApi {

    @ApiOperation(value = "Create Domestic Standing Order Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "DomesticStandingOrderConsent object representing the consent created",
                         response = DomesticStandingOrderConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/domestic-standing-order-consents",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<DomesticStandingOrderConsent> createConsent(@ApiParam(value = "Create Consent Request", required = true)
                                                               @Valid
                                                               @RequestBody CreateDomesticStandingOrderConsentRequest request);


    @ApiOperation(value = "Get Domestic Standing Order Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "DomesticStandingOrderConsent object representing the consent created",
                         response = DomesticStandingOrderConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/domestic-standing-order-consents/{consentId}",
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.GET)
    ResponseEntity<DomesticStandingOrderConsent> getConsent(@PathVariable(value = "consentId") String consentId,
                                                            @RequestHeader(value = "x-api-client-id") String apiClientId);


    @ApiOperation(value = "Authorise Domestic Standing Order Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "DomesticStandingOrderConsent object representing the consent created",
                         response = DomesticStandingOrderConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/domestic-standing-order-consents/{consentId}/authorise",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<DomesticStandingOrderConsent> authoriseConsent(@PathVariable(value = "consentId") String consentId,
                                                                  @ApiParam(value = "Authorise Consent Request", required = true)
                                                                  @Valid
                                                                  @RequestBody AuthorisePaymentConsentRequest request);


    @ApiOperation(value = "Reject Domestic Standing Order Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "DomesticStandingOrderConsent object representing the consent created",
                         response = DomesticStandingOrderConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/domestic-standing-order-consents/{consentId}/reject",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<DomesticStandingOrderConsent> rejectConsent(@PathVariable(value = "consentId") String consentId,
                                                               @ApiParam(value = "Reject Consent Request", required = true)
                                                               @Valid
                                                               @RequestBody RejectConsentRequest request);



    @ApiOperation(value = "Consume Domestic Standing Order Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "DomesticStandingOrderConsent object representing the consent created",
                         response = DomesticStandingOrderConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/domestic-standing-order-consents/{consentId}/consume",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<DomesticStandingOrderConsent> consumeConsent(@PathVariable(value = "consentId") String consentId,
                                                                @ApiParam(value = "Consume Consent Request", required = true)
                                                                @Valid
                                                                @RequestBody ConsumePaymentConsentRequest request);

}
