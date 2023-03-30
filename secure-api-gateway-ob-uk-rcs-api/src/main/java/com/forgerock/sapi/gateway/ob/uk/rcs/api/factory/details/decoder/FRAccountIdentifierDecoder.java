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
package com.forgerock.sapi.gateway.ob.uk.rcs.api.factory.details.decoder;

import static com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.IDENTIFICATION;
import static com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.NAME;
import static com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.SCHEME_NAME;
import static com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.SECONDARY_IDENTIFICATION;
import static com.forgerock.sapi.gateway.ob.uk.rcs.api.json.utils.JsonUtilValidation.isNotNull;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Decoder of FRAccountIdentifier objects
 */
@Component
public class FRAccountIdentifierDecoder implements GsonDecoder<FRAccountIdentifier> {

    @Override
    public FRAccountIdentifier decode(JsonElement jsonElement) {
        Objects.requireNonNull(jsonElement, "jsonElement must be supplied");
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        return FRAccountIdentifier.builder()
                .identification(jsonObject.get(IDENTIFICATION).getAsString())
                .name(jsonObject.get(NAME).getAsString())
                .schemeName(jsonObject.get(SCHEME_NAME).getAsString())
                .secondaryIdentification(isNotNull(jsonObject.get(SECONDARY_IDENTIFICATION)) ?
                        jsonObject.get(SECONDARY_IDENTIFICATION).getAsString() :
                        null)
                .build();
    }
}
