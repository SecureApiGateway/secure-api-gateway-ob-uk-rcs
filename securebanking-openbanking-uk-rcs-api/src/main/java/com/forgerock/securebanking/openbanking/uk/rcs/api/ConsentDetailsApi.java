/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetails;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "RCS Consent Details API", description = "The RCS Consent Request Details API")
public interface ConsentDetailsApi {

    @ApiOperation(value = "Get consent details", notes = "Get the consent details behind a consent request JWT." +
            " Due to the size of the consent request JWT, we are using a POST instead of a GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consent details", response = ConsentDetails.class)
    })
    @RequestMapping(value = "/api/rcs/consent/details",
            consumes = {"application/jwt; charset=utf-8", "application/json; charset=utf-8"},
            produces = {"application/json; charset=utf-8"},
            method = RequestMethod.POST)
    ResponseEntity<ConsentDetails> getConsentDetails(
            @ApiParam(value = "Consent request JWT received by AM", required = true)
            @RequestBody String consentRequestJwt,

            @ApiParam(value = "Cookie containing the user session", required = true)
            @CookieValue(value = "${am.cookie.name}") String ssoToken) throws OBErrorException;
}
