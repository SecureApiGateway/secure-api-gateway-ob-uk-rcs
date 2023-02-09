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
package com.forgerock.securebanking.openbanking.uk.rcs.api;

import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.securebanking.openbanking.uk.rcs.swagger.SwaggerApiTags;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Interface for dealing with the PSU's consent decision,
 */
@Api(tags = {SwaggerApiTags.CONSENT_DECISION_TAG})
public interface ConsentDecisionApi {

    @ApiOperation(
            value = "Submit consent decision",
            notes = "Submit the PSU's consent decision for a corresponding consent request")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A redirection response, specifying the URI the PSU should be redirected to",
                    response = RedirectionAction.class)
    })
    @RequestMapping(value = "/rcs/api/consent/decision",
            consumes = {"application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<RedirectionAction> submitConsentDecision(
            @ApiParam(value = "Consent decision JWT", required = true)
            @RequestBody String consentDecisionSerialised) throws OBErrorException;
}
