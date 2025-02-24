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

import jakarta.annotation.PostConstruct;

import org.joda.time.DateTimeZone;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ConsentStoreConfiguration.class)
public class ConsentStoreRepoTestApp {

    @PostConstruct
    void postConstruct() {
        // This is needed to make data generated in the unit tests UTC, which makes equality checking easier as Mongo will return data in UTC (and DateTime.equals using different timezones is false)
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

}
