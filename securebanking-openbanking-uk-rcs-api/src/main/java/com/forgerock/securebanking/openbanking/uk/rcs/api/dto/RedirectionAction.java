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
package com.forgerock.securebanking.openbanking.uk.rcs.api.dto;

import lombok.*;
import org.springframework.http.HttpMethod;

/**
 * Simple POJO to send the redirection information from the server side to the UI for RS accounts and payments.
 */
@ToString
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedirectionAction {

    private String consentJwt;
    private String redirectUri;
    private HttpMethod requestMethod;
    private String errorMessage;

//    public RedirectionAction() {
//    }
//
//    public RedirectionAction(String consentJwt, String redirectUri, HttpMethod requestMethod, String errorMessage) {
//        this.consentJwt = consentJwt;
//        this.redirectUri = redirectUri;
//        this.requestMethod = requestMethod;
//        this.errorMessage = errorMessage;
//    }
//
//    public String getConsentJwt() {
//        return consentJwt;
//    }
//
//    public String getRedirectUri() {
//        return redirectUri;
//    }
//
//    public HttpMethod getRequestMethod() {
//        return requestMethod;
//    }

//    public static Builder builder() {
//        return new Builder();
//    }

//    public static class Builder {
//
//        private String consentJwt;
//        private String redirectUri;
//        private HttpMethod requestMethod = HttpMethod.POST;
//        private
//        public Builder consentJwt(String consentJwt) {
//            this.consentJwt = consentJwt;
//            return this;
//        }
//
//        public Builder redirectUri(String redirectUri) {
//            this.redirectUri = redirectUri;
//            return this;
//        }
//
//        public Builder requestMethod(HttpMethod requestMethod) {
//            this.requestMethod = requestMethod;
//            return this;
//        }
//
//        public Builder errorMessage(String errorMessage) {
//            this.errorMessage = requestMethod;
//            return this;
//        }
//
//        public RedirectionAction build() {
//            return new RedirectionAction(consentJwt, redirectUri, requestMethod, errorMessage);
//        }
//    }
}
