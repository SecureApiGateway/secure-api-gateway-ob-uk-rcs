/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.securebanking.platform.client.models.accounts;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.platform.client.models.general.ConsentRequest;
import com.forgerock.securebanking.platform.client.models.general.User;
import com.nimbusds.jwt.SignedJWT;
import lombok.Builder;

import java.util.List;

/**
 * Represents the required information to provide the details of a consent.
 */
public class AccountConsentRequest extends ConsentRequest {
    List<FRAccountWithBalance> accounts;

    @Builder
    public AccountConsentRequest(String intentId, SignedJWT consentRequestJwt, User user, String clientId, List<FRAccountWithBalance> accounts) {
        super(intentId, consentRequestJwt, user, clientId);
        this.accounts = accounts;
    }
}
