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
package com.forgerock.securebanking.openbanking.uk.rcs.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.dto.consent.FRPaymentConsent;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.forgerock.securebanking.openbanking.uk.rcs.testsupport.WireMockServerExtension.WIRE_MOCK_SERVER;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.OK;

@Component
public class WireMockStubHelper {

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    public void stubGetUserProfile(Map<String, String> responseBody) {
        WIRE_MOCK_SERVER.stubFor(post(urlEqualTo("/am/userprofile"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseBody))
                        .withStatus(OK.value())));
    }

    @SneakyThrows
    public void stubGetUserAccounts(List<FRAccountWithBalance> responseBody) {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/rs/backoffice/accounts/search/findByUserId.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseBody))
                        .withStatus(OK.value())));
    }

    @SneakyThrows
    public void stubGetPaymentConsent(FRPaymentConsent responseBody) {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/idm/payment-consents/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseBody))
                        .withStatus(OK.value())));
    }

    @SneakyThrows
    public void stubGetTpp(Tpp responseBody) {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/idm/repo/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseBody))
                        .withStatus(OK.value())));
    }

    public void stubSignClaims(String responseBody) {
        WIRE_MOCK_SERVER.stubFor(post(urlEqualTo("/jwkms/rcs/signresponse"))
                .willReturn(aResponse()
                        .withBody(responseBody)
                        .withStatus(OK.value())));
    }

    public void stubRcsResponseToAm(String uriPath) {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching(uriPath + ".*"))
                .willReturn(aResponse()
                        .withHeader("Location", "http://www.google.com")
                        .withStatus(FOUND.value())));
    }

    public void stubUpdatePaymentConsent(FRPaymentConsent expectedRequestBody) {
        WIRE_MOCK_SERVER.stubFor(put(urlPathMatching("/idm/payment-consents/.*"))
                .withRequestBody(containing("\"accountId\":\"" + expectedRequestBody.getAccountId() + "\""))
                // Note upper case OB JSON format
                .withRequestBody(containing("\"Status\":\"" + expectedRequestBody.getData().getStatus().toString() + "\""))
                .withRequestBody(containing("\"resourceOwnerUsername\":\"" + expectedRequestBody.getResourceOwnerUsername() + "\""))
                .withRequestBody(containing("\"oauth2ClientId\":\"" + expectedRequestBody.getOauth2ClientId() + "\""))
                .willReturn(aResponse()
                        .withStatus(OK.value())));
    }
}
