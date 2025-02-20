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

import static java.util.Objects.requireNonNull;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

/**
 * Implementation of {@link ApiVersionValidator} which enforces that only backwards compatibility is supported
 * i.e. a resource can be accessed by the current API version or a future API version only.
 */
public class BackwardsCompatibilityApiVersionValidator implements ApiVersionValidator {

    @Override
    public boolean canAccessResourceUsingApiVersion(OBVersion creationVersion, OBVersion accessVersion) {
        requireNonNull(creationVersion, "creationVersion must be provided");
        requireNonNull(accessVersion, "accessVersion must be provided");
        return creationVersion == accessVersion || creationVersion.isBeforeVersion(accessVersion);
    }
}
