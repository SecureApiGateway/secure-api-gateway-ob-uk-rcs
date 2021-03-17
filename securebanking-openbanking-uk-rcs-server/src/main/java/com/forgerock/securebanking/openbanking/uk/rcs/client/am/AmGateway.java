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
package com.forgerock.securebanking.openbanking.uk.rcs.client.am;

import com.forgerock.securebanking.openbanking.uk.rcs.configuration.AmConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.net.HttpHeaders.*;

@Service
@Slf4j
public class AmGateway {

    private final RestTemplate restTemplate;
    private final AmConfigurationProperties amConfiguration;

    public AmGateway(RestTemplate restTemplate, AmConfigurationProperties amConfiguration) {
        this.restTemplate = restTemplate;
        this.amConfiguration = amConfiguration;
    }

    public ResponseEntity sendToAm(String uri,
                                   HttpMethod method,
                                   HttpHeaders additionalHttpHeaders,
                                   ParameterizedTypeReference<String> parameterizedTypeReference,
                                   String body) {
        return sendToAm(uri, true, method, additionalHttpHeaders, parameterizedTypeReference, body);
    }

    public ResponseEntity sendToAm(String uri,
                                   boolean sendXForwardingHeader,
                                   HttpMethod method,
                                   HttpHeaders additionalHttpHeaders,
                                   ParameterizedTypeReference<String> parameterizedTypeReference,
                                   Object body) {
        additionalHttpHeaders.remove("host");
        if (sendXForwardingHeader) {
            additionalHttpHeaders.add("host", amConfiguration.getHostName());
            additionalHttpHeaders.remove(X_FORWARDED_PORT);
            additionalHttpHeaders.remove(X_FORWARDED_HOST);
            additionalHttpHeaders.remove(X_FORWARDED_PROTO);
            additionalHttpHeaders.add(X_FORWARDED_HOST, amConfiguration.getHostName());
            additionalHttpHeaders.add(X_FORWARDED_PROTO, "https");
        }

        // TODO - add SecurityContextHolder certificate related code? See AMGateway in forgerock-openbanking-am

        HttpEntity httpEntity = new HttpEntity(body, additionalHttpHeaders);
        UriComponentsBuilder builder;
        try {
            builder = UriComponentsBuilder
                    .fromUriString(uri)
                    .uri(new URI(amConfiguration.getAuthorizeEndpoint()));
        } catch (URISyntaxException e) {
            throw new RuntimeException("AM path path is not a URI", e);
        }
        log.debug("Redirect URI before UriComponentsBuilder.build(): {}", uri);
        uri = builder.build().toUriString();
        log.debug("Redirect URI after UriComponentsBuilder.build(): {}", uri);

        //BUG in the UriComponentsBuilder which encode twice
        uri = uri.replace("%20", " ");
        log.debug("Sending request to AM with uri '{}' and headers : '{}'", uri, additionalHttpHeaders);

        try {
            return restTemplate.exchange(uri, method, httpEntity, parameterizedTypeReference);
        } catch (HttpStatusCodeException e) {
            log.error("An error happened on the AS: {}", e.getResponseBodyAsString(), e);
            throw e;
        }
    }
}
