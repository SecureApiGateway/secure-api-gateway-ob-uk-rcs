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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.jwt.RcsJwtSigner;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;

@Configuration
public class RcsApplicationConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public RestTemplate restTemplate(@Qualifier("mappingJacksonHttpMessageConverter") MappingJackson2HttpMessageConverter converter) {
        RestTemplate restTemplate = new RestTemplate();
        customiseRestTemplate(converter, restTemplate);
        return restTemplate;
    }

    private void customiseRestTemplate(@Qualifier("mappingJacksonHttpMessageConverter") MappingJackson2HttpMessageConverter converter, RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        messageConverters.add(converter);
        // support for http PATCH calls
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate.setRequestFactory(requestFactory);
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter(@Qualifier("objectMapperBuilderCustomizer") Jackson2ObjectMapperBuilderCustomizer objectMapperBuilderCustomizer) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        Jackson2ObjectMapperBuilder objectMapperBuilder = new Jackson2ObjectMapperBuilder();
        objectMapperBuilderCustomizer.customize(objectMapperBuilder);
        converter.setObjectMapper(objectMapperBuilder.build());
        return converter;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer objectMapperBuilderCustomizer() {
        return (jacksonObjectMapperBuilder) -> {
            jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
            jacksonObjectMapperBuilder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            jacksonObjectMapperBuilder.featuresToEnable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL);
            jacksonObjectMapperBuilder.modules(new JodaModule());
            jacksonObjectMapperBuilder.dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ"));
        };
    }

    @Bean
    public RcsJwtSigner rcsJwtSigner(@Value("${rcs.consent.response.jwt.signingKeyId}") String signingKeyId,
                                     @Value("${rcs.consent.response.jwt.signingAlgorithm:PS256}") String signingAlgorithm,
                                     @Value("${rcs.consent.response.jwt.privateKeyPath}") Path privateKeyPath) throws IOException, JOSEException {

        logger.info("Configuring JWT Signer - keyId: {}, signingAlgo: {}, privateKeyPath: {}", signingKeyId, signingAlgorithm, privateKeyPath);
        final JWK jwk = JWK.parseFromPEMEncodedObjects(Files.readString(privateKeyPath));
        if (!(jwk instanceof RSAKey)) {
            throw new IllegalArgumentException("private key file must contain an RSA Private Key");
        }
        final JWSSigner signer = new RSASSASigner((RSAKey) jwk);
        return new RcsJwtSigner(signingKeyId, JWSAlgorithm.parse(signingAlgorithm), signer);
    }
}
