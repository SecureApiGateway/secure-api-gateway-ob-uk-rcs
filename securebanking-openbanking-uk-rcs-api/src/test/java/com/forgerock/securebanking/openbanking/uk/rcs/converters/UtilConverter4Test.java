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
package com.forgerock.securebanking.openbanking.uk.rcs.converters;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalPermissionsCode;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import static com.forgerock.securebanking.platform.client.test.support.AccountAccessConsentDetailsTestFactory.gson;

public class UtilConverter4Test {

    public static final String ACCOUNT_INTENT_ID = "AAC_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    public static final String DOMESTIC_SCHEDULED_PAYMENT_INTENT_ID = "PDSC_1c214525-d0c8-4d13-xxx-b812c6fafabe";
    public static final String DOMESTIC_STANDING_ORDER_PAYMENT_INTENT_ID = "PDSOC_1c214525-d0c8-4d13-xxx-b812c6fafabe";


    public static final JsonElement transformationForPermissionsList(List<FRExternalPermissionsCode> list) {
        if (list == null || list.isEmpty())
        {
            return null;
        }
        List<String> permissions = new ArrayList<>();
        for (FRExternalPermissionsCode element : list) {
            permissions.add(element.getValue());
        }
        return gson.toJsonTree(permissions);
    }
}
