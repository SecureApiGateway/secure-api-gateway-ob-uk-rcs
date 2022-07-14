/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.configuration.swagger;

import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.security.Principal;

import static springfox.documentation.builders.PathSelectors.any;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

/**
 * Configuration class for the application's Swagger specification.
 */
@Configuration
@EnableSwagger2
@ConfigurationProperties(prefix = "swagger")
@Data
public class SwaggerConfiguration {

    public String title;
    public String description;
    public String license;
    public String licenseUrl;
    public String termsOfServiceUrl;
    public String contactName;
    public String contactUrl;
    public String docketApisBasePackage;
    public String docketPathsSelectorRegex;

    /**
     * Default docket for swagger documentation
     * @return Docket
     */
    @Bean
    public Docket customDefault() {
        return new Docket(SWAGGER_2)
                .ignoredParameterTypes(Principal.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage(docketApisBasePackage))
                .paths(PathSelectors.regex(docketPathsSelectorRegex))
                .build()
                .directModelSubstitute(org.joda.time.LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(org.joda.time.DateTime.class, java.util.Date.class)
                .apiInfo(apiInfo());
    }

    /**
     * Api information to display on swagger UI
     * @return ApiInfo
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(title)
                .description(description)
                .license(license)
                .licenseUrl(licenseUrl)
                .termsOfServiceUrl(termsOfServiceUrl)
                .contact(new Contact(contactName, contactUrl, Strings.EMPTY))
                .build();
    }
}
