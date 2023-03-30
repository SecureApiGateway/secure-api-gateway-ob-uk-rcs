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
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class FRAccountIdentifierDecoderTest {

    private final FRAccountIdentifierDecoder decoder = new FRAccountIdentifierDecoder();

    @Test
    void failsIfInputNotJsonObject() {
        final NullPointerException nullPointerException = assertThrows(NullPointerException.class, () -> decoder.decode(null));
        assertEquals("jsonElement must be supplied", nullPointerException.getMessage());
        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> decoder.decode(new JsonArray()));
        assertEquals("Not a JSON Object: []", illegalStateException.getMessage());
    }

    @Test
    void decodesValidObject() {
        final String expectedIdentification = "identification123";
        final String expectedName = "Current Account";
        final String expectedSchema = "Schema XYZ";
        final String expectedSecondaryId = "secondaryId123";

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(IDENTIFICATION, expectedIdentification);
        jsonObject.addProperty(NAME, expectedName);
        jsonObject.addProperty(SCHEME_NAME, expectedSchema);
        jsonObject.addProperty(SECONDARY_IDENTIFICATION, expectedSecondaryId);

        validateFRAccountIdentifier(decoder.decode(jsonObject), expectedIdentification, expectedName, expectedSchema, expectedSecondaryId);

        // Secondary identification field is optional
        jsonObject.remove(SECONDARY_IDENTIFICATION);
        validateFRAccountIdentifier(decoder.decode(jsonObject), expectedIdentification, expectedName, expectedSchema, null);
    }

    private static void validateFRAccountIdentifier(FRAccountIdentifier accountIdentifier, String expectedIdentification,
                                                    String expectedName, String expectedSchema, String expectedSecondaryId) {

        assertEquals(expectedName, accountIdentifier.getName());
        assertEquals(expectedIdentification, accountIdentifier.getIdentification());
        assertEquals(expectedSchema, accountIdentifier.getSchemeName());
        assertEquals(expectedSecondaryId, accountIdentifier.getSecondaryIdentification());
    }

}