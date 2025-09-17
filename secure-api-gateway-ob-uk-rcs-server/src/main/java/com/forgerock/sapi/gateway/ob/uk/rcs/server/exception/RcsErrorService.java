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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.exception;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType;
import com.google.common.base.Splitter;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Map;

import static com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBConstants.OIDCClaim.CONSENT_APPROVAL_REDIRECT_URI;
import static com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBConstants.OIDCClaim.STATE;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType.INVALID_REQUEST;
import static com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ErrorType.SERVER_ERROR;

/**
 * This service is intended for scenarios where the consent provided by the TPP is invalid so we cannot proceed with
 * PSU authorisation.
 */
@Service
@Slf4j
public class RcsErrorService {

    /**
     * Intended for scenarios where the consent provided by the TPP is invalid so we cannot proceed with PSU
     * authorisation. In this situation, we want to wrap the error message inside a redirect back to the provided
     * callback URL as that the TPP will be aware of the issue and the PSU can receive a more helpful response.
     *
     * @param consentContextJwt A {@link String} representing the JWT provided in the consent request from TPP.
     * @param obriErrorType The type of error.
     * @param args The arguments to add to the parameterized error message.
     * @return The HTTP response for the RCS UI.
     * @throws OBErrorException If we cannot handle the provided consent JWT or extract the TPP redirect URL we have
     * to fall back to an error response to the UI.
     */
    public ResponseEntity<RedirectionAction> invalidConsentError(String consentContextJwt,
                                                                 OBRIErrorType obriErrorType,
                                                                 Object... args) throws OBErrorException {
        return invalidConsentError(consentContextJwt, new OBErrorException(obriErrorType, args));
    }

    /**
     *
     * @param invalidConsentException Exception object to populate the response
     * @return The HTTP response for the RCS UI.
     */
    public ResponseEntity<RedirectionAction> invalidConsentError(InvalidConsentException invalidConsentException) {

        return invalidConsentError(invalidConsentException.getConsentRequestJwt(), invalidConsentException);
    }

    public ResponseEntity<RedirectionAction> invalidConsentError(String consentContextJwt, InvalidConsentException invalidConsentException) {
        ErrorType errorType = invalidConsentException.getErrorType();
        OBRIErrorType obriErrorType = invalidConsentException.getObriErrorType();
        try {
            Map<String, String> params = extractParams(consentContextJwt);
            log.debug("Consent context JWT {} has error '{}'", consentContextJwt, invalidConsentException.getReason());

            // Consent content may come in the form of a 'request' claim (parameter), which is more secure and should
            // take precedence, or may come as top-level claims (e.g. 'redirect_uri') as per the OAuth2 specs.
            JWTClaimsSet requestParamClaims = getRequestParameterClaims(params);

            // Identify error redirect_uri
            String redirectURL = requestParamClaims.getStringClaim("redirect_uri");
            if (redirectURL == null) {
                redirectURL = params.get("redirect_uri");
            }
            log.debug("Consent redirect_uri {}", redirectURL);
            redirectURL = redirectURL != null ? URLDecoder.decode(redirectURL, StandardCharsets.UTF_8) : null;

            if (redirectURL == null || redirectURL.isEmpty()) {
                String message = "Null or empty redirect URL. Falling back to just throwing error back to UI";
                String errorMessage = obriErrorType != null ? String.format(obriErrorType.getMessage(), message) : message;
                log.warn(errorMessage);
                return ResponseEntity
                        .status(INVALID_REQUEST.getHttpStatus().value())
                        .body(RedirectionAction.builder()
                                .errorMessage(errorMessage)
                                .build());
            }

            // Identify error state
            String state = requestParamClaims.getStringClaim(STATE);
            if (state == null) {
                state = params.get(STATE);
            }
            log.debug("Consent state {}", state);
            state = state != null ? URLDecoder.decode(state, StandardCharsets.UTF_8) : "";

            String errorDescription = invalidConsentException.getReason();
            UriComponents uriComponents = UriComponentsBuilder
                    .fromHttpUrl(redirectURL)
                    .fragment("error=" + errorType.getErrorCode() + "&state=" + state +
                            "&error_description=" + String.format(errorDescription) +
                            "&error_uri=" + errorType.getErrorUri(errorType.getInternalCode()))
                    .encode()
                    .build();

            return ResponseEntity
                    .status(errorType.getHttpStatus())
                    .body(RedirectionAction.builder()
                            .consentJwt(consentContextJwt)
                            .redirectUri(uriComponents.toUriString())
                            .errorMessage(invalidConsentException.getErrorType().getDescription())
                            .requestMethod(HttpMethod.GET.name())
                            .build());
        } catch (Exception e) {
            String message = String.format("Failed to turn error into a redirect back to TPP with and Exception. " +
                    "Falling back to just throwing error back to UI. %s", e.getMessage());
            String errorMessage = obriErrorType != null ? String.format(obriErrorType.getMessage(), message) : message;
            log.warn(errorMessage, e);
            return ResponseEntity
                    .status(SERVER_ERROR.getHttpStatus().value())
                    .body(RedirectionAction.builder()
                            .errorMessage(errorMessage)
                            .build());
        }
    }

    public ResponseEntity<RedirectionAction> invalidConsentError(String consentContextJwt, OBErrorException obError)
            throws OBErrorException {
        try {
            Map<String, String> params = extractParams(consentContextJwt);
            log.debug("Consent context JWT {} has OBErrorException '{}'", consentContextJwt, obError.getMessage());

            // Consent content may come in the form of a 'request' claim (parameter), which is more secure and should
            // take precedence, or may come as top-level claims (e.g. 'redirect_uri') as per the OAuth2 specs.
            JWTClaimsSet requestParamClaims = getRequestParameterClaims(params);

            // Identify error redirect_uri
            String redirectURL = requestParamClaims.getStringClaim("redirect_uri");
            if (redirectURL == null) {
                redirectURL = params.get("redirect_uri");
            }
            log.debug("Consent redirect_uri {}", redirectURL);
            redirectURL = redirectURL != null ? URLDecoder.decode(redirectURL, StandardCharsets.UTF_8) : null;

            if (redirectURL == null || redirectURL.isEmpty()) {
                log.warn("Null or empty redirect URL. Falling back to just throwing error back to UI");
                throw obError;
            }

            // Identify error state
            String state = requestParamClaims.getStringClaim(STATE);
            if (state == null) {
                state = params.get(STATE);
            }
            log.debug("Consent state {}", state);
            state = state != null ? URLDecoder.decode(state, StandardCharsets.UTF_8) : "";

            UriComponents uriComponents = UriComponentsBuilder
                    .fromHttpUrl(redirectURL)
                    .fragment("error=invalid_request_object&state=" + state + "&error_description=" +
                            String.format(obError.getObriErrorType().getMessage(), obError.getArgs()))
                    .encode()
                    .build();

            return ResponseEntity
                    .status(obError.getObriErrorType().getHttpStatus())
                    .body(RedirectionAction.builder()
                            .redirectUri(uriComponents.toUriString())
                            .requestMethod(HttpMethod.GET.name())
                            .build());
        } catch (Exception e) {
            log.warn("Failed to turn error into a redirect back to TPP with and Exception. Falling back to just " +
                    "throwing error back to UI", e);
            throw obError;
        }
    }

    private Map<String, String> extractParams(String consentContextJwt) throws ParseException {
        log.debug("Parse consent request JWS: {}", consentContextJwt);
        SignedJWT signedJWT = (SignedJWT) JWTParser.parse(consentContextJwt);
        log.debug("Get claim: {} from JWT: {}", CONSENT_APPROVAL_REDIRECT_URI, signedJWT.getParsedString());
        String amRedirectUri = signedJWT.getJWTClaimsSet()
                .getStringClaim(CONSENT_APPROVAL_REDIRECT_URI);
        log.debug("Get TPP callback URL from AM URL: {}", amRedirectUri);

        String query = amRedirectUri.split("\\?")[1];
        return Splitter.on('&').trimResults().withKeyValueSeparator("=").split(query);
    }

    private JWTClaimsSet getRequestParameterClaims(Map<String, String> params) throws ParseException {
        log.debug("Checking presence of 'request' (parameter) claim in params {}", params);
        String requestParamValue = params.get("request");
        if (requestParamValue == null) {
            // If not present then return empty claimsSet
            log.debug("No 'request' parameter found");
            return new JWTClaimsSet.Builder().build();
        }
        SignedJWT signedJWT = (SignedJWT) JWTParser.parse(requestParamValue);
        JWTClaimsSet claimSet = signedJWT.getJWTClaimsSet();
        log.debug("Identified 'request' parameter claims {}", claimSet);
        return claimSet;
    }
}
