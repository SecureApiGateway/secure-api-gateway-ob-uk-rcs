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
package com.forgerock.sapi.gateway.rcs.consent.store.repo;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.MongoRepoPackageMarker;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.forgerock.sapi.gateway.uk.common.shared.spring.converter.DateToUTCDateTimeConverter;

@Configuration
@ComponentScan(basePackageClasses = ConsentStoreConfiguration.class)
@EnableMongoRepositories(basePackageClasses = MongoRepoPackageMarker.class)
@EnableMongoAuditing
public class ConsentStoreConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Control which IntentTypes use this module via config
     */
    @Value("${consent.store.enabled.intentTypes}")
    private EnumSet<IntentType> enabledIntentTypes;

    @PostConstruct
    public void logEnabledIntentTypes() {
        logger.info("IntentTypes configured to use the Consent Store Module: {}", enabledIntentTypes);
    }

    @Bean
    public EnumSet<IntentType> getEnabledIntentTypes() {
        return enabledIntentTypes;
    }

    /**
     * Create MongoCustomConversions instance with our custom data type converters.
     * <p>
     * {@link DateToUTCDateTimeConverter} is installed to convert Date objects in Mongo documents to joda DateTime
     * objects specifying UTC timezone.
     * Without this converter, the joda default converter will use the system's timezone instead which leads to issues
     * when calling equals() on OB data-model objects due to TZ mismatch with data received via the REST API (which is always UTC).
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        final List<Converter> customConverters = List.of(new DateToUTCDateTimeConverter());
        logger.info("Installing custom converters for Mongo: {}", customConverters);
        return new MongoCustomConversions(customConverters);
    }
}
