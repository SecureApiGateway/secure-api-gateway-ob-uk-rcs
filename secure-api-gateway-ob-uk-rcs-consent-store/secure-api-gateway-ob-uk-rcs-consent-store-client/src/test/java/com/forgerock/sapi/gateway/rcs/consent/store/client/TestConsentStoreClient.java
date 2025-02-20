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
package com.forgerock.sapi.gateway.rcs.consent.store.client;

import java.text.SimpleDateFormat;

import jakarta.annotation.PostConstruct;
import uk.org.openbanking.jackson.DateTimeDeserializer;
import uk.org.openbanking.jackson.DateTimeSerializer;
import uk.org.openbanking.jackson.LocalDateDeserializer;
import uk.org.openbanking.jackson.LocalDateSerializer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.forgerock.sapi.gateway.rcs.consent.store.api.ConsentStoreApiConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreConfiguration;

/**
 * Test Application which starts the Consent Store REST API
 */
@SpringBootApplication
@Import({ConsentStoreConfiguration.class, ConsentStoreApiConfiguration.class})
public class TestConsentStoreClient {

    @PostConstruct
    void postConstruct() {
        // This is needed to make data generated in the unit tests UTC, which makes equality checking easier as Mongo will return data in UTC (and DateTime.equals using different timezones is false)
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer objectMapperBuilderCustomizer() {
        return (jacksonObjectMapperBuilder) -> {
            jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
            jacksonObjectMapperBuilder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            jacksonObjectMapperBuilder.featuresToEnable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL);
            jacksonObjectMapperBuilder.modules(new JodaModule());
            jacksonObjectMapperBuilder.dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ"));
            jacksonObjectMapperBuilder.deserializerByType(DateTime.class, new DateTimeDeserializer());
            jacksonObjectMapperBuilder.serializerByType(DateTime.class, new DateTimeSerializer(DateTime.class));
            jacksonObjectMapperBuilder.deserializerByType(LocalDate.class, new LocalDateDeserializer());
            jacksonObjectMapperBuilder.serializerByType(LocalDate.class, new LocalDateSerializer(LocalDate.class));
        };
    }
}