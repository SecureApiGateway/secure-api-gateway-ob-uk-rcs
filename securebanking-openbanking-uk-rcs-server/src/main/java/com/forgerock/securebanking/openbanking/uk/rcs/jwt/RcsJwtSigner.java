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
package com.forgerock.securebanking.openbanking.uk.rcs.jwt;

import java.util.Objects;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;

public class RcsJwtSigner {

    private final String signingKeyId;
    private final JWSAlgorithm signingAlgorithm;
    private final JWSSigner signer;

    public RcsJwtSigner(String signingKeyId, JWSAlgorithm signingAlgorithm, JWSSigner signer) {
        Objects.requireNonNull(signingKeyId, "signingKeyId must be supplied");
        Objects.requireNonNull(signingAlgorithm, "signingAlgorithm must be supplied");
        Objects.requireNonNull(signer, "signer must be supplied");
        this.signingKeyId = signingKeyId;
        this.signingAlgorithm = signingAlgorithm;
        this.signer = signer;
    }

    public String signPayload(JWTClaimsSet jwtClaimsSet) throws JOSEException {
        Objects.requireNonNull(jwtClaimsSet, "jwtClaimsSet must be supplied");
        final JWSObject jwsObject = new JWSObject(new JWSHeader.Builder(signingAlgorithm).keyID(signingKeyId).build(),
                                                  new Payload(jwtClaimsSet.toJSONObject()));
        jwsObject.sign(signer);
        return jwsObject.serialize();
    }

}
