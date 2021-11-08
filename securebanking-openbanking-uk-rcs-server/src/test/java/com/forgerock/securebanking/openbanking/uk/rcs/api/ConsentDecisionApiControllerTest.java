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
package com.forgerock.securebanking.openbanking.uk.rcs.api;

import com.forgerock.securebanking.openbanking.uk.rcs.RcsApplicationTestSupport;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.ConsentDecisionRequest;
import com.forgerock.securebanking.openbanking.uk.rcs.testsupport.JwtTestHelper;
import com.forgerock.securebanking.platform.client.Constants;
import com.forgerock.securebanking.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.platform.client.models.AccountConsentDetails;
import com.forgerock.securebanking.platform.client.models.ConsentDecision;
import com.forgerock.securebanking.platform.client.services.ConsentServiceClient;
import com.forgerock.securebanking.platform.client.services.JwkServiceClient;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.aValidAccountConsentDetails;
import static com.forgerock.securebanking.platform.client.test.support.ConsentDecisionTestDataFactory.aValidAccountConsentDecision;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Spring Boot Test for {@link ConsentDecisionApiController}.
 */
// TODO: first approach to tests the controller
@EnableConfigurationProperties
@ActiveProfiles("test")
@SpringBootTest(classes = RcsApplicationTestSupport.class, webEnvironment = RANDOM_PORT)
public class ConsentDecisionApiControllerTest {

    @LocalServerPort
    private int port;

    private static final String BASE_URL = "http://localhost:";
    private static final String CONTEXT_DETAILS_URI = "/api/rcs/consent/decision";

    @MockBean
    private ConsentServiceClient consentServiceClient;

    @MockBean
    private JwkServiceClient jwkServiceClient;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void ShouldGetRedirectionAction() throws ExceptionClient {
        // given
        ConsentDecision consentDecision = aValidAccountConsentDecision();
        AccountConsentDetails accountConsentDetails = aValidAccountConsentDetails(consentDecision.getIntentId());
        given(consentServiceClient.updateConsent(consentDecision)).willReturn(accountConsentDetails);
        String jwt = JwtTestHelper.consentRequestJwt(
                consentDecision.getClientId(),
                consentDecision.getIntentId(),
                consentDecision.getResourceOwnerUsername()
        );

        given(jwkServiceClient.signClaims(any(JWTClaimsSet.class), anyString())).willReturn(jwt);
        String consentDetailURL = BASE_URL + port + CONTEXT_DETAILS_URI;

        ConsentDecisionRequest consentDecisionRequest = ConsentDecisionRequest.builder()
                .sharedAccounts(accountConsentDetails.getAccountIds())
                .consentJwt(jwt)
                .decision(Constants.ConsentDecision.AUTHORISED)
                .build();
        HttpEntity<String> request = new HttpEntity(consentDecisionRequest, headers());

        // when
        ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDetailURL, request, RedirectionAction.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getConsentJwt()).isNotEmpty();
        assertThat(response.getBody().getRedirectUri()).isNotEmpty();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        headers.add("Cookie", "iPlanetDirectoryPro=aSsoToken");
        return headers;
    }

}
