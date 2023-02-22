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

import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.IntentType;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;

/**
 * Interface to create {@link ConsentDetailsFactory} factories
 * To create a new Consent details factory:<br/>
 * <lu>
 *     <li>Implement this interface</li>
 *     <li>Just annotate it with {@link Component}</li>
 * </lu>
 * @param <T> extends {@link ConsentDetails}
 */
public interface ConsentDetailsFactory<T extends ConsentDetails> {
    /**
     * Decode a json into an instance of type {@link ConsentDetails}
     * @param json {@link JsonObject} to be decoded
     * @return an instance of type {@link ConsentDetails}
     */
    T decode(JsonObject json);

    /**
     * The intent type to identify the factory to be provided by {@link ConsentDetailsFactoryProvider}
     * @return {@link IntentType}
     */
    IntentType getIntentType();
}
