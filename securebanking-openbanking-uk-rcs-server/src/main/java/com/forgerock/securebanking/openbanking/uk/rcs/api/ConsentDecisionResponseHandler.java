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
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.securebanking.openbanking.uk.rcs.client.am.AmGateway;
import com.forgerock.securebanking.openbanking.uk.rcs.client.jwkms.JwkmsApiClient;
import com.forgerock.securebanking.openbanking.uk.rcs.configuration.AmConfigurationProperties;
import com.forgerock.securebanking.openbanking.uk.rcs.configuration.RcsConfigurationProperties;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.common.api.meta.OBConstants.OIDCClaim.CONSENT_APPROVAL_REDIRECT_URI;
import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.RCS_CONSENT_RESPONSE_FAILURE;
import static com.forgerock.securebanking.openbanking.uk.rcs.common.RcsConstants.Claims.CSRF;
import static com.forgerock.securebanking.openbanking.uk.rcs.common.RcsConstants.Claims.SCOPES;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

/**
 * RCS flows contains the functions needed by RCS to communicate with the AS.
 */
@Component
@Slf4j
public class ConsentDecisionResponseHandler {

    private final RcsConfigurationProperties rcsConfigurationProperties;
    private final AmConfigurationProperties amConfiguration;
    private final JwkmsApiClient jwkmsApiClient;
    private final AmGateway amGateway;

    public ConsentDecisionResponseHandler(RcsConfigurationProperties rcsConfigurationProperties,
                                          AmConfigurationProperties amConfiguration,
                                          JwkmsApiClient jwkmsApiClient,
                                          AmGateway amGateway) {
        this.rcsConfigurationProperties = rcsConfigurationProperties;
        this.amConfiguration = amConfiguration;
        this.jwkmsApiClient = jwkmsApiClient;
        this.amGateway = amGateway;
    }

    public ResponseEntity<RedirectionAction> handleResponse(String ssoToken,
                                                            SignedJWT consentContextJwt,
                                                            boolean decision,
                                                            JWTClaimsSet jwtClaimsSet,
                                                            String clientId) throws ParseException, OBErrorException {
        log.debug("Redirect the resource owner to the original oauth2/openid request, but this time with the " +
                "consent response jwt '{}'.", consentContextJwt);

        String csrf = jwtClaimsSet.getStringClaim(CSRF);
        List<String> scopes = new ArrayList<>(jwtClaimsSet.getJSONObjectClaim(SCOPES).keySet());
        String redirectUri = jwtClaimsSet.getStringClaim(CONSENT_APPROVAL_REDIRECT_URI);

        String consentJwt = generateRcsConsentResponse(csrf, decision, scopes, clientId);

        ResponseEntity responseEntity = sendRcsResponseToAM(ssoToken, RedirectionAction.builder()
                .redirectUri(redirectUri)
                .consentJwt(consentJwt)
                .requestMethod(POST)
                .build());
        log.debug("Response received from AM: {}", responseEntity);

        if (responseEntity.getStatusCode() != HttpStatus.FOUND) {
            log.error("When sending the consent response '{}' to AM, it failed to returned a 302", responseEntity);
            throw new OBErrorException(RCS_CONSENT_RESPONSE_FAILURE);
        }

        String location = responseEntity.getHeaders().getFirst("Location");
        log.debug("The redirection to the consent page should be in the location '{}'", location);

        return ResponseEntity.ok(RedirectionAction.builder()
                .redirectUri(location)
                .build());
    }

    /**
     * Generates a new RCS authentication JWT.
     *
     * @return a JWT that can be used to authenticate RCS to the AS.
     */
    private String generateRcsConsentResponse(String csrf, boolean decision, List<String> scopes, String clientId) {
        JWTClaimsSet.Builder requestParameterClaims;
        requestParameterClaims = new JWTClaimsSet.Builder();
        requestParameterClaims.issuer(rcsConfigurationProperties.getIssuerId());
        requestParameterClaims.audience(amConfiguration.getIssuerId());
        requestParameterClaims.expirationTime(new Date(new Date().getTime() + Duration.ofMinutes(5).toMillis()));
        requestParameterClaims.claim("decision", decision);
        requestParameterClaims.claim("csrf", csrf);
        requestParameterClaims.claim("scopes", scopes);
        requestParameterClaims.claim("clientId", clientId);

        // TODO - can the RCS sign the JWT instead? Or call IG?
        return jwkmsApiClient.signClaims(requestParameterClaims.build());
    }

    private ResponseEntity sendRcsResponseToAM(String ssoToken, RedirectionAction redirectionAction) {
        HttpHeaders amHeaderRcsResponse = new HttpHeaders();
        amHeaderRcsResponse.add("Cookie", amConfiguration.getCookieName() + "=" + ssoToken);
        amHeaderRcsResponse.add("Content-Type", "application/x-www-form-urlencoded");

        log.debug("Consent response {} to {} to uri '{}'", redirectionAction.getConsentJwt(),
                redirectionAction.getRequestMethod(), redirectionAction.getRedirectUri());
        String body = "consent_response=" + redirectionAction.getConsentJwt();

        String url = fromHttpUrl(redirectionAction.getRedirectUri()).build(true).toUri().toString();
        log.debug("Redirect URL: {}", url);
        ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<>() {};
        return amGateway.sendToAm(url, POST, amHeaderRcsResponse, typeReference, body);
    }
}
