/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs;

import static com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.FRCustomerInfoTestHelper.aValidFRCustomerInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.RCSServerApplicationTestSupport;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.RsConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.RsResourceApiConfiguration;

/**
 * Unit test for {@link CustomerInfoService}
 */

@SpringBootTest(classes = RCSServerApplicationTestSupport.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class CustomerInfoServiceTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RsConfiguration rsConfiguration;

    @Autowired
    private RsResourceApiConfiguration rsResourceApiConfiguration;

    @Autowired
    private CustomerInfoService customerInfoService;

    @Autowired
    private MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter;

    private MockRestServiceServer mockServer;
    private String baseUri;

    @BeforeEach
    public void setup() {
        baseUri = rsConfiguration.getBaseUri();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void shouldReturnValidFRCustomerInfo() throws Exception {
        // Given
        String userId = UUID.randomUUID().toString();
        FRCustomerInfo customerInfoResponseExpected = aValidFRCustomerInfo(userId);
        String findByUserIdUriContext = rsResourceApiConfiguration.getCustomerInfo().get(
                RsResourceApiConfiguration.Operation.FIND_USER_BY_ID.toString()
        );

        mockServer.expect(
                        ExpectedCount.once(), requestTo(baseUri + findByUserIdUriContext + "?userId=" + userId)
                )
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                mappingJacksonHttpMessageConverter.getObjectMapper().writeValueAsString(customerInfoResponseExpected)
                        )
                );
        // When
        Optional<FRCustomerInfo> customerInfo = customerInfoService.getCustomerInformation(userId);
        // Then
        mockServer.verify();
        assertThat(customerInfo.isPresent()).isTrue();
        assertEquals(customerInfo.get(), customerInfoResponseExpected);
    }

    @Test
    void shouldReturnCustomerInfoNotFound() {
        // Given
        String userId = UUID.randomUUID().toString();
        String findByUserIdUriContext = rsResourceApiConfiguration.getCustomerInfo().get(
                RsResourceApiConfiguration.Operation.FIND_USER_BY_ID.toString()
        );

        mockServer.expect(
                        ExpectedCount.once(), requestTo(baseUri + findByUserIdUriContext + "?userId=" + userId)
                )
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        // When
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> customerInfoService.getCustomerInformation(userId));

        // Then
        mockServer.verify();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
