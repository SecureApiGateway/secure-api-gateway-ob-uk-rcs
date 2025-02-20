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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.version;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

/**
 * Validator which checks if a resource (typically a Consent) can be accessed using a particular API version.
 */
public interface ApiVersionValidator {

    /**
     * Tests if a resource can be accessed using a particular API version.
     *
     * @param creationVersion the version of the API used to create the resource
     * @param accessVersion   the version of the API used to access the resource
     * @return true if the resource can be accessed with the given version.
     */
    boolean canAccessResourceUsingApiVersion(OBVersion creationVersion, OBVersion accessVersion);

}
