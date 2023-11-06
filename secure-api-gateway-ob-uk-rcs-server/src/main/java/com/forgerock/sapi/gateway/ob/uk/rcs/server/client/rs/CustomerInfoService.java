/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.RsConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.RsResourceApiConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

/**
 * Service to request customer information operations to Test facility Bank (RS) resources api
 */
@Service
@Slf4j
public class CustomerInfoService extends BaseRsClient {
    private final RsResourceApiConfiguration rsResourceApiConfiguration;

    public CustomerInfoService(
            RestTemplate restTemplate,
            RsConfiguration rsConfiguration,
            RsResourceApiConfiguration rsResourceApiConfiguration
    ) {
        super(restTemplate, rsConfiguration);
        this.rsResourceApiConfiguration = rsResourceApiConfiguration;
    }

    public Optional<FRCustomerInfo> getCustomerInformation(String userId) {
        log.debug("Making a request to RS to retrieve customer information details with user Id: {}", userId);

        ResponseEntity<FRCustomerInfo> entity = restTemplate.exchange(
                getFindByUserIdOperationUri(userId),
                GET,
                createRequestEntity(),
                FRCustomerInfo.class
        );
        return Optional.ofNullable(entity.getBody());
    }

    private URI getFindByUserIdOperationUri(String userId) {
        UriComponentsBuilder builder = fromHttpUrl(
                rsConfiguration.getBaseUri() +
                        rsResourceApiConfiguration.getCustomerInfo().get(
                                RsResourceApiConfiguration.Operation.FIND_USER_BY_ID.toString()
                        )
        );
        return builder.queryParam("userId", userId).build().encode().toUri();
    }
}
