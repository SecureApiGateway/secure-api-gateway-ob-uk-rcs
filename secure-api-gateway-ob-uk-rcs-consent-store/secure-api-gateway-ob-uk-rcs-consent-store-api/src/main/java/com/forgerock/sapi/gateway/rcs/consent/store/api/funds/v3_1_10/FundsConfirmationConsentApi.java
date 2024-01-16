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
package com.forgerock.sapi.gateway.rcs.consent.store.api.funds.v3_1_10;

import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.AuthoriseFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.CreateFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;

import javax.validation.Valid;

@Validated
@Api(tags = {"v3.1.10"})
@RequestMapping(value = "/consent/store/v3.1.10")
public interface FundsConfirmationConsentApi {


    @ApiOperation(value = "Create Funds Confirmation Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "FundsConfirmationConsent object representing the consent created",
                    response = FundsConfirmationConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/funds-confirmation-consents",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<FundsConfirmationConsent> createConsent(@ApiParam(value = "Create Consent Request", required = true)
                                                           @Valid
                                                           @RequestBody CreateFundsConfirmationConsentRequest request);


    @ApiOperation(value = "Get Funds Confirmation Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "FundsConfirmationConsent object representing the consent created",
                    response = FundsConfirmationConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/funds-confirmation-consents/{consentId}",
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.GET)
    ResponseEntity<FundsConfirmationConsent> getConsent(@PathVariable(value = "consentId") String consentId,
                                                        @RequestHeader(value = "x-api-client-id") String apiClientId);


    @ApiOperation(value = "Authorise Funds Confirmation Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "FundsConfirmationConsent object representing the consent created",
                    response = FundsConfirmationConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/funds-confirmation-consents/{consentId}/authorise",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<FundsConfirmationConsent> authoriseConsent(@PathVariable(value = "consentId") String consentId,
                                                              @ApiParam(value = "Authorise Consent Request", required = true)
                                                              @Valid
                                                              @RequestBody AuthoriseFundsConfirmationConsentRequest request);


    @ApiOperation(value = "Reject Funds Confirmation Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "FundsConfirmationConsent object representing the consent created",
                    response = FundsConfirmationConsent.class),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/funds-confirmation-consents/{consentId}/reject",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<FundsConfirmationConsent> rejectConsent(@PathVariable(value = "consentId") String consentId,
                                                           @ApiParam(value = "Reject Consent Request", required = true)
                                                           @Valid
                                                           @RequestBody RejectConsentRequest request);


    @ApiOperation(value = "Delete Funds Confirmation Consent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Delete successful"),
            @ApiResponse(code = 400, message = "Bad request", response = OBErrorResponse1.class),
            @ApiResponse(code = 403, message = "Forbidden", response = OBErrorResponse1.class),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 406, message = "Not Acceptable"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = OBErrorResponse1.class)
    })
    @RequestMapping(value = "/funds-confirmation-consents/{consentId}",
            method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteConsent(@PathVariable(value = "consentId") String consentId,
                                       @RequestHeader(value = "x-api-client-id") String apiClientId);


}
