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
package com.forgerock.sapi.gateway.ob.uk.rcs.api.factory.details;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

/**
 * Provides an implementation of {@link ConsentDetailsFactory}
 */
@Component
@Slf4j
public class ConsentDetailsFactoryProvider {

    private final EnumMap<IntentType, ConsentDetailsFactory> consentDetailsFactories;

    /**
     * Spring populates the list with all known objects of type {@link ConsentDetailsFactory} annotated as {@link Component}
     * @param consentDetailsFactoryList a list of type {@link ConsentDetailsFactory}
     */
    @Autowired
    public ConsentDetailsFactoryProvider(List<ConsentDetailsFactory> consentDetailsFactoryList) {
        this.consentDetailsFactories = new EnumMap<>(IntentType.class);
        for (ConsentDetailsFactory consentDetailsFactory : consentDetailsFactoryList) {
            this.consentDetailsFactories.put(consentDetailsFactory.getIntentType(), consentDetailsFactory);
            log.debug(
                    "ConsentDetailsFactory {}:{} added to {}",
                    consentDetailsFactory.getIntentType(),
                    consentDetailsFactory.getClass().getSimpleName(),
                    this.getClass().getSimpleName());
        }
    }

    /**
     * Provides a {@link ConsentDetailsFactory} instance by {@link IntentType}
     * @param intentType {@link IntentType}
     * @return a {@link ConsentDetailsFactory} instance
     */
    public ConsentDetailsFactory getFactory(IntentType intentType) {
        return consentDetailsFactories.get(intentType);
    }
}
