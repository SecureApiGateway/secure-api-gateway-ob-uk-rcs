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
package com.forgerock.sapi.gateway.rcs.consent.store.repo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.MongoRepoPackageMarker;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentServiceFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.version.ApiVersionValidator;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.version.BackwardsCompatibilityApiVersionValidator;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.forgerock.sapi.gateway.uk.common.shared.spring.converter.JodaTimeConverters;

import jakarta.annotation.PostConstruct;

@Configuration
@ComponentScan(basePackageClasses = ConsentStoreConfiguration.class)
@EnableMongoRepositories(basePackageClasses = MongoRepoPackageMarker.class)
@EnableMongoAuditing
@Order(value = Ordered.HIGHEST_PRECEDENCE)
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
     * Create MongoCustomConversions instance with Joda Time converters
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        logger.info("Installing joda time converters for Mongo");
        return new MongoCustomConversions(new ArrayList<>(JodaTimeConverters.getConvertersToRegister()));
    }

    @Bean
    public ApiVersionValidator apiVersionValidator() {
        return new BackwardsCompatibilityApiVersionValidator();
    }

    /**
     * Dynamically creates and registers the internal ConsentService objects. The internal services are used by
     * RCS code which is not being invoked by the Consent Store REST API i.e. for internal use cases where there is
     * no requirement to check an API version.
     * <p>
     * Currently, the internal services are used for the Consent Details and Decision UI functionality.
     * <p>
     * The beans registered will be named as follows: internal$ConsentServiceInterfaceClassName e.g. internalAccountAccessConsentService
     * <p>
     * In order to autowire these services, components need to be annotated with @DependsOn({"internalConsentServices"})
     * to ensure that these services have been created prior to the component being constructed.
     *
     * @param consentServiceFactories the factories used to create the services
     * @param beanFactory the bean factory to register the created services are beans
     * @return List of created services
     */
    @Bean
    public List<BaseConsentService> internalConsentServices(List<ConsentServiceFactory> consentServiceFactories, ConfigurableListableBeanFactory beanFactory) {
        return consentServiceFactories.stream().map(consentServiceFactory -> {
            BaseConsentService internalConsentService = consentServiceFactory.createInternalConsentService();
            final String beanName = "internal" + getConsentServiceClassNameFromFactoryClass(consentServiceFactory.getClass());

            // initializeBean is important as it ensures that the services are proxied by Spring, which enables it to add
            // the runtime support for validation annotations i.e. @Validated and @Valid (and any other annotations supported by Spring).
            internalConsentService = (BaseConsentService) beanFactory.initializeBean(internalConsentService, beanName);
            beanFactory.registerSingleton(beanName, internalConsentService);
            beanFactory.autowireBean(internalConsentService);
            return internalConsentService;
        }).toList();
    }

    public static String getConsentServiceClassNameFromFactoryClass(Class<? extends ConsentServiceFactory> clazz) {
        return clazz.getSimpleName().replace("Factory", "");
    }
}
