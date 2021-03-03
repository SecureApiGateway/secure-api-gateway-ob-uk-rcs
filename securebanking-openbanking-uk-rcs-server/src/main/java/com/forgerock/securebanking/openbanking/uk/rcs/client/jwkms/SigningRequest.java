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
package com.forgerock.securebanking.openbanking.uk.rcs.client.jwkms;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.OBConstants;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SigningRequest {

    public static final Set<String> DEFAULT_SUPPORT_CRIT_CLAIMS = Stream.of(
            OBConstants.OBJwtHeaderClaims.B64,
            OBConstants.OBJwtHeaderClaims.OB_ISS,
            OBConstants.OBJwtHeaderClaims.OB_IAT,
            OBConstants.OBJwtHeaderClaims.OB_TAN
    ).collect(Collectors.toSet());

    @Builder.Default
    private CustomHeaderClaims customHeaderClaims = CustomHeaderClaims.builder().build();

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomHeaderClaims {
        @Builder.Default private boolean includeB64 = false;
        @Builder.Default private boolean includeOBIat = false;
        @Builder.Default private boolean includeOBIss = false;
        @Builder.Default private String tan = null;
        @Builder.Default private boolean includeCrit = false;
    }
}