/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.client.rs;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomestic;
import com.forgerock.securebanking.openbanking.uk.rcs.configuration.RcsConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
@Slf4j
public class DomesticPaymentService {
    private static final String FIND_USER_BY_ID_URI = "/backoffice/domestic-payments/search/findByUserId";

    private final RestTemplate restTemplate;
    private final RcsConfigurationProperties configurationProperties;

    public DomesticPaymentService(RestTemplate restTemplate, RcsConfigurationProperties configurationProperties) {
        this.restTemplate = restTemplate;
        this.configurationProperties = configurationProperties;
    }

    public List<FRWriteDomestic> getDomesticPayments(String userID) {
        // This is necessary as auth server always uses lowercase user id
        String lowercaseUserId = userID.toLowerCase();
        log.debug("Searching for domestic payments with user ID: {}", lowercaseUserId);

        ParameterizedTypeReference<List<FRWriteDomestic>> ptr = new ParameterizedTypeReference<>() {
        };
        UriComponentsBuilder builder = fromHttpUrl(configurationProperties.getProtocol() + configurationProperties.getRsFqdn() + FIND_USER_BY_ID_URI);
        builder.queryParam("userId", lowercaseUserId);

        URI uri = builder.build().encode().toUri();
        ResponseEntity<List<FRWriteDomestic>> entity = restTemplate.exchange(uri, GET, null, ptr);
        return entity.getBody();
    }
}
