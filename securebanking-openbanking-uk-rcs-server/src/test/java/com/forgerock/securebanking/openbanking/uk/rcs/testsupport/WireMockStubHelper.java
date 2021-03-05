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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRDomesticPaymentConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
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
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/rs/api/accounts/search/findByUserId.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseBody))
                        .withStatus(OK.value())));
    }

    @SneakyThrows
    public void stubGetPaymentConsent(FRDomesticPaymentConsent responseBody) {
        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/idm/domestic-payment-consents/.*"))
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
        WIRE_MOCK_SERVER.stubFor(post(urlEqualTo("/jwkms/api/crypto/signClaims"))
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

    public void stubUpdatePaymentConsent(FRDomesticPaymentConsent expectedRequestBody) {
        WIRE_MOCK_SERVER.stubFor(put(urlPathMatching("/idm/domestic-payment-consents/.*"))
                .withRequestBody(containing("\"accountId\":\"" + expectedRequestBody.getAccountId() + "\""))
                .withRequestBody(containing("\"status\":\"" + expectedRequestBody.getStatus().name() + "\""))
                .withRequestBody(containing("\"userId\":\"" + expectedRequestBody.getUserId() + "\""))
                .withRequestBody(containing("\"pispId\":\"" + expectedRequestBody.getPispId() + "\""))
                .willReturn(aResponse()
                        .withStatus(OK.value())));
    }
}
