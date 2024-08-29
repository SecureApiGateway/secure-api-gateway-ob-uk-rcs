/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rcs.consent.store.api;

import static com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreConfiguration.getConsentServiceClassNameFromFactoryClass;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentServiceFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo.CustomerInfoConsentServiceFactory;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

@Configuration
@ComponentScan(basePackageClasses = ConsentStoreApiConfiguration.class)
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class ConsentStoreApiConfiguration {

    // Default to 24hours
    @Value("${consent.idempotency.expiration.seconds:86400}")
    private int idempotencyExpirationDuration;

    @Bean
    public Supplier<DateTime> getIdempotencyExpirationSupplier() {
        return () -> DateTime.now().plusSeconds(idempotencyExpirationDuration);
    }

    private List<OBVersion> supportedApiVersions() {
        return List.of(OBVersion.v3_1_10, OBVersion.v4_0_0);
    }

    /**
     * Dynamically creates and registers the versioned ConsentService objects, one service is created per supported
     * OBVersion. These services are then wired into the ConsentApiControllers and will validate that consents are
     * being accessed only by API versions that are supported, see {@link com.forgerock.sapi.gateway.rcs.consent.store.repo.version.ApiVersionValidator}
     * <p>
     * The beans registered will be named as follows: $apiVersion$ConsentServiceInterfaceClassName e.g. v3.1.10AccountAccessConsentService
     * <p>
     * In order to autowire these services, components need to be annotated with @DependsOn({"versionedConsentServices"})
     * to ensure that these services have been created prior to the component being constructed.
     *
     * @param consentServiceFactories the factories used to create the services
     * @param beanFactory the bean factory to register the created services are beans
     * @return List of created services
     */
    @Bean
    public List<BaseConsentService> versionedConsentServices(List<ConsentServiceFactory> consentServiceFactories, ConfigurableListableBeanFactory beanFactory) {
        final List<OBVersion> apiVersions = supportedApiVersions();
        final List<BaseConsentService> consentServices = new ArrayList<>(consentServiceFactories.size() * apiVersions.size());

        boolean customerInfoConsentServiceFactoryFound = false;
        for (ConsentServiceFactory consentServiceFactory : consentServiceFactories) {
            // Customer Info is versioned differently to standard OB APIs, only a single version: v1.0 is supported.
            if (consentServiceFactory instanceof CustomerInfoConsentServiceFactory) {
                if (!customerInfoConsentServiceFactoryFound) {
                    consentServices.add(createConsentServiceBean(beanFactory, consentServiceFactory, OBVersion.v1_0));
                    customerInfoConsentServiceFactoryFound = true;
                }
                continue;
            }
            for (OBVersion apiVersion : apiVersions) {
                consentServices.add(createConsentServiceBean(beanFactory, consentServiceFactory, apiVersion));
            }
        }

        return consentServices;
    }

    private static BaseConsentService createConsentServiceBean(ConfigurableListableBeanFactory beanFactory,
                                                               ConsentServiceFactory consentServiceFactory,
                                                               OBVersion apiVersion) {
        BaseConsentService apiConsentService = consentServiceFactory.createApiConsentService(apiVersion);
        final String beanName = apiVersion.getCanonicalName() + getConsentServiceClassNameFromFactoryClass(consentServiceFactory.getClass());

        // initializeBean is important as it ensures that the services are proxied by Spring, which enables it to add
        // the runtime support for validation annotations i.e. @Validated and @Valid (and any other annotations supported by Spring).
        apiConsentService = (BaseConsentService) beanFactory.initializeBean(apiConsentService, beanName);
        beanFactory.registerSingleton(beanName, apiConsentService);
        beanFactory.autowireBean(apiConsentService);
        return apiConsentService;
    }
}
