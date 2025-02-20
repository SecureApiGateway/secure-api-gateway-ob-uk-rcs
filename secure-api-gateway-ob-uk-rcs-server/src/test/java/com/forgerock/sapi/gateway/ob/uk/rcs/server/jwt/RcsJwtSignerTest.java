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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;

class RcsJwtSignerTest {

    @Test
    void failToConstructIfMissingAnyParam() {
        assertEquals("signingKeyId must be supplied", assertThrows(NullPointerException.class,
                () -> new RcsJwtSigner(null, JWSAlgorithm.PS256, mock(RSASSASigner.class))).getMessage());
        assertEquals("signingAlgorithm must be supplied", assertThrows(NullPointerException.class,
                () -> new RcsJwtSigner("signingKeyId", null, mock(RSASSASigner.class))).getMessage());
        assertEquals("signer must be supplied", assertThrows(NullPointerException.class,
                () -> new RcsJwtSigner("signingKeyId", JWSAlgorithm.PS256, null)).getMessage());
    }

    @Test
    void failToConstructIfSigningAlgoNotSupported() throws Exception {
        final String kid = UUID.randomUUID().toString();
        final RSAKey signingKey = new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE)
                                                                .keyID(kid)
                                                                .generate();
        final RSASSASigner signer = new RSASSASigner(signingKey);

        // Try to use ES signing algo with RSA signer
        final JWSAlgorithm signingAlgorithm = JWSAlgorithm.ES256;
        assertEquals("signingAlgorithm: ES256 not supported by supplied JWSSigner",
                assertThrows(IllegalStateException.class, () -> new RcsJwtSigner(kid, signingAlgorithm, signer)).getMessage());
    }

    @Test
    void failToSignNullClaims() {
        final RSASSASigner mockSigner = mock(RSASSASigner.class);
        when(mockSigner.supportedJWSAlgorithms()).thenReturn(Set.of(JWSAlgorithm.PS256));
        final RcsJwtSigner jwtSigner = new RcsJwtSigner("signingKid", JWSAlgorithm.PS256, mockSigner);
        assertEquals("jwtClaimsSet must be supplied", assertThrows(NullPointerException.class,
                () -> jwtSigner.createSignedJwt(null)).getMessage());
    }

    @Test
    void producesValidSignedJwt() throws Exception {
        final String kid = UUID.randomUUID().toString();
        final RSAKey signingKey = new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE)
                                                                .keyID(kid)
                                                                .generate();
        final RSASSASigner signer = new RSASSASigner(signingKey);
        final JWSAlgorithm signingAlgorithm = JWSAlgorithm.PS256;
        final RcsJwtSigner jwtSigner = new RcsJwtSigner(kid, signingAlgorithm, signer);
        final String jti = UUID.randomUUID().toString();
        final String aud = "audience";
        final String customClaim = "claim1";
        final String customClaimValue = "value1";
        final JWTClaimsSet claimsSet = new Builder().jwtID(jti).audience(aud).claim(customClaim, customClaimValue).build();
        final String signedPayload = jwtSigner.createSignedJwt(claimsSet);

        final JWSObject decodedPayload = JWSObject.parse(signedPayload);
        assertEquals(signingAlgorithm, decodedPayload.getHeader().getAlgorithm());
        assertEquals(kid, decodedPayload.getHeader().getKeyID());

        final JWTClaimsSet decodedClaimsSet = JWTClaimsSet.parse(decodedPayload.getPayload().toJSONObject());
        assertEquals(jti, decodedClaimsSet.getJWTID());
        assertEquals(List.of(aud), decodedClaimsSet.getAudience());
        assertEquals(customClaimValue, decodedClaimsSet.getClaim(customClaim));

        JWSVerifier verifier = new RSASSAVerifier(signingKey);
        assertTrue(decodedPayload.verify(verifier));
    }
}
