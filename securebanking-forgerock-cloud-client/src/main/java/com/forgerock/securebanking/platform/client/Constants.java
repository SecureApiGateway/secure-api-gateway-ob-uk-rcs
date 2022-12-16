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
package com.forgerock.securebanking.platform.client;

public class Constants {

    public static class Claims {
        public static final String INTENT_ID = "openbanking_intent_id";
        public static final String CLIENT_ID = "clientId";
        public static final String USER_NAME = "username";
        public static final String ACR = "acr";
        public static final String C_HASH = "c_hash";
        public static final String S_HASH = "s_hash";
        public static final String ID_TOKEN = "id_token";
        public static final String USER_INFO = "user_info";
        public static final String CLAIMS = "claims";
    }

    public static class ConsentDecisionStatus {
        public static final String AUTHORISED = "Authorised";
        public static final String REJECTED = "Rejected";
        public static final String REVOKED = "Revoked";
    }

    public static class URLParameters {
        public static final String CONSENT_RESPONSE = "consent_response";
        public static final String INTENT_ID = "@IntentId@";
        public static final String CLIENT_ID = "@ClientId@";
        public static final String USER_ID = "@UserId@";
    }
}
