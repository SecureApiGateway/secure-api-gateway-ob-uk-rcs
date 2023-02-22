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
package com.forgerock.sapi.gateway.ob.uk.rcs.api.dto;

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
}
