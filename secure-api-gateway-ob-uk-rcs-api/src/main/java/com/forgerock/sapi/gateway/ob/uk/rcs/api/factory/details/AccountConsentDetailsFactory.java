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

import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.json.utils.JsonUtilValidation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static java.util.Objects.requireNonNull;

/**
 * Account consent details factory implements {@link ConsentDetailsFactory}
 */
@Component
public class AccountConsentDetailsFactory implements ConsentDetailsFactory<AccountsConsentDetails> {

    @Override
    public AccountsConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        AccountsConsentDetails details = new AccountsConsentDetails();
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (JsonUtilValidation.isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                details.setFromTransaction(
                        JsonUtilValidation.isNotNull(data.get(TRANSACTION_FROM_DATETIME)) ?
                                Instant.parse(data.get(TRANSACTION_FROM_DATETIME).getAsString()).toDateTime() :
                                null
                );

                details.setToTransaction(
                        JsonUtilValidation.isNotNull(data.get(TRANSACTION_TO_DATETIME)) ?
                                Instant.parse(data.get(TRANSACTION_TO_DATETIME).getAsString()).toDateTime() :
                                null
                );

                details.setExpiredDate(
                        JsonUtilValidation.isNotNull(data.get(EXPIRATION_DATETIME)) ?
                                Instant.parse(data.get(EXPIRATION_DATETIME).getAsString()).toDateTime() :
                                null
                );

                if (JsonUtilValidation.isNotNull(data.get(PERMISSIONS))) {
                    details.setPermissions(decodePermissions(data.getAsJsonArray(PERMISSIONS)));
                }
            }
        }
        return details;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.ACCOUNT_ACCESS_CONSENT;
    }

    private List<FRExternalPermissionsCode> decodePermissions(JsonArray permissions) {
        List<FRExternalPermissionsCode> permissionsCodeList = new ArrayList<>();
        if (permissions == null || permissions.size() == 0)
            return null;
        else {
            for (JsonElement permission : permissions) {
                permissionsCodeList.add(FRExternalPermissionsCode.fromValue(permission.getAsString()));
            }
        }
        return permissionsCodeList;
    }
}
