/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.jwt;

import java.util.Objects;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Produces signed JWTs for the RCS
 */
public class RcsJwtSigner {

    private final JWSHeader jwsHeader;

    private final JWSSigner signer;

    /**
     * @param signingKeyId     String kid of the key that will be used to sign JWTs
     * @param signingAlgorithm JWSAlgorithm the signing algorithm used to sign the JWTs
     * @param signer           JWSSigner signer that is responsible for doing the signing.
     *                         Must be able to support the signingAlgorithm
     */
    public RcsJwtSigner(String signingKeyId, JWSAlgorithm signingAlgorithm, JWSSigner signer) {
        Objects.requireNonNull(signingKeyId, "signingKeyId must be supplied");
        Objects.requireNonNull(signingAlgorithm, "signingAlgorithm must be supplied");
        Objects.requireNonNull(signer, "signer must be supplied");
        this.jwsHeader = new JWSHeader.Builder(signingAlgorithm).keyID(signingKeyId).build();
        this.signer = signer;
        if (!signer.supportedJWSAlgorithms().contains(signingAlgorithm)) {
            throw new IllegalStateException("signingAlgorithm: " + signingAlgorithm
                    + " not supported by supplied JWSSigner");
        }
    }

    /**
     * Produces a serialized signed JWT String containing the supplied claims.
     * All messages are signed in the same way, with the same key and algorithm.
     *
     * @param jwtClaimsSet JWTClaimSet the claims to add to the payload of the JWT that we are signing
     * @return Serialized signed JWT String
     * @throws JOSEException
     */
    public String createSignedJwt(JWTClaimsSet jwtClaimsSet) throws JOSEException {
        Objects.requireNonNull(jwtClaimsSet, "jwtClaimsSet must be supplied");
        final JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(jwtClaimsSet.toJSONObject()));
        jwsObject.sign(signer);
        return jwsObject.serialize();
    }
}
