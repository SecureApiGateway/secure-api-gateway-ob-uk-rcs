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

import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.RsBackofficeConfiguration;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.RsConfiguration;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
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
public class AccountService {

    private final RestTemplate restTemplate;
    private final RsConfiguration rsConfiguration;
    private final RsBackofficeConfiguration rsBackofficeConfiguration;

    public AccountService(
            RestTemplate restTemplate,
            RsConfiguration rsConfiguration,
            RsBackofficeConfiguration rsBackofficeConfiguration
    ) {
        this.restTemplate = restTemplate;
        this.rsConfiguration = rsConfiguration;
        this.rsBackofficeConfiguration = rsBackofficeConfiguration;
    }

    public List<FRAccount> getAccounts(String userID) {
        // This is necessary as auth server always uses lowercase user id
        String lowercaseUserId = userID.toLowerCase();
        log.debug("Searching for accounts with user ID: {}", lowercaseUserId);

        ParameterizedTypeReference<List<FRAccount>> ptr = new ParameterizedTypeReference<>() {
        };
        UriComponentsBuilder builder = fromHttpUrl(
                rsConfiguration.getBaseUri() +
                        rsBackofficeConfiguration.getAccounts().get(
                                RsBackofficeConfiguration.UriContexts.FIND_USER_BY_ID.toString()
                        )
        );
        builder.queryParam("userId", lowercaseUserId);

        URI uri = builder.build().encode().toUri();
        ResponseEntity<List<FRAccount>> entity = restTemplate.exchange(uri, GET, null, ptr);
        return entity.getBody();
    }

    public List<FRAccountWithBalance> getAccountsWithBalance(String userID) {
        // This is necessary as auth server always uses lowercase user id
        String lowercaseUserId = userID.toLowerCase();
        log.debug("Searching for accounts with balance for user ID: {}", lowercaseUserId);

        ParameterizedTypeReference<List<FRAccountWithBalance>> ptr = new ParameterizedTypeReference<>() {
        };
        UriComponentsBuilder builder = fromHttpUrl(
                rsConfiguration.getBaseUri() +
                        rsBackofficeConfiguration.getAccounts().get(
                                RsBackofficeConfiguration.UriContexts.FIND_USER_BY_ID.toString()
                        )
        );
        builder.queryParam("userId", lowercaseUserId);
        builder.queryParam("withBalance", true);

        URI uri = builder.build().encode().toUri();
        ResponseEntity<List<FRAccountWithBalance>> entity = restTemplate.exchange(uri, GET, null, ptr);
        return entity.getBody();
    }

    public FRAccountWithBalance getAccountWithBalanceByIdentifiers(String userID, String name, String identification, String schemeName) {
        // This is necessary as auth server always uses lowercase user id
        String lowercaseUserId = userID.toLowerCase();
        log.debug("Searching for accounts with balance for user ID: {}", lowercaseUserId);
        ParameterizedTypeReference<FRAccountWithBalance> ptr = new ParameterizedTypeReference<>() {
        };

        UriComponentsBuilder builder = fromHttpUrl(
                rsConfiguration.getBaseUri() +
                        rsBackofficeConfiguration.getAccounts().get(
                                RsBackofficeConfiguration.UriContexts.FIND_BY_ACCOUNT_IDENTIFIERS.toString()
                        )
        );

        builder.queryParam("userId", lowercaseUserId);
        builder.queryParam("name", name);
        builder.queryParam("identification", identification);
        builder.queryParam("schemeName", schemeName);

        URI uri = builder.build().encode().toUri();
        ResponseEntity<FRAccountWithBalance> entity = restTemplate.exchange(uri, GET, null, ptr);
        return entity.getBody();
    }

    public FRAccountIdentifier getAccountIdentifier(String userID, String name, String identification, String schemeName) {
        // This is necessary as auth server always uses lowercase user id
        String lowercaseUserId = userID.toLowerCase();
        log.debug("Searching for accounts with balance for user ID: {}", lowercaseUserId);
        ParameterizedTypeReference<FRAccountIdentifier> ptr = new ParameterizedTypeReference<>() {
        };

        UriComponentsBuilder builder = fromHttpUrl(
                rsConfiguration.getBaseUri() +
                        rsBackofficeConfiguration.getAccounts().get(
                                RsBackofficeConfiguration.UriContexts.FIND_BY_ACCOUNT_IDENTIFIERS.toString()
                        )
        );

        builder.queryParam("userId", lowercaseUserId);
        builder.queryParam("name", name);
        builder.queryParam("identification", identification);
        builder.queryParam("schemeName", schemeName);

        URI uri = builder.build().encode().toUri();
        ResponseEntity<FRAccountIdentifier> entity = restTemplate.exchange(uri, GET, null, ptr);
        return entity.getBody();
    }
}