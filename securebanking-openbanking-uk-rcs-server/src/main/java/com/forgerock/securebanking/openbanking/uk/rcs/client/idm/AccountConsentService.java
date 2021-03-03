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
package com.forgerock.securebanking.openbanking.uk.rcs.client.idm;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountConsent;
import com.forgerock.securebanking.openbanking.uk.rcs.configuration.RcsConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
public class AccountConsentService {
    private static final String CONSENTS_URI = "/account-access-consents";

    private final RcsConfigurationProperties configurationProperties;
    private final RestTemplate restTemplate;

    public AccountConsentService(RcsConfigurationProperties configurationProperties, RestTemplate restTemplate) {
        this.configurationProperties = configurationProperties;
        this.restTemplate = restTemplate;
    }

    public FRAccountConsent getAccountConsent(String consentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        // TODO - add additional required headers
        return restTemplate.getForObject(consentIdUrl(consentId), FRAccountConsent.class, headers);
    }

    public void updateAccountConsent(FRAccountConsent accountRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        // TODO - add additional required headers
        HttpEntity<FRAccountConsent> requestEntity = new HttpEntity<>(accountRequest, headers);
        restTemplate.exchange(consentIdUrl(accountRequest.getId()), PUT, requestEntity, Void.class);
    }

    private String consentUrl() {
        return configurationProperties.getIdmBaseUrl() + CONSENTS_URI;
    }

    private String consentIdUrl(String consentId) {
        return consentUrl() + "/" + consentId;
    }
}
