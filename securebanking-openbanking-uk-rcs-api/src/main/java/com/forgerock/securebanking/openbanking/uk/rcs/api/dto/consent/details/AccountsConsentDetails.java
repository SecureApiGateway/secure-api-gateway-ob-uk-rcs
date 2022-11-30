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
package com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Models the consent data that is used for an account details request.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class AccountsConsentDetails extends ConsentDetails {

    private List<FRExternalPermissionsCode> permissions;
    private DateTime fromTransaction;
    private DateTime toTransaction;
    private DateTime expiredDate;

    @Override
    public AccountsConsentDetails getInstance() {
        return new AccountsConsentDetails();
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.ACCOUNT_ACCESS_CONSENT;
    }

    @Override
    public void mapping(JsonObject consentDetails) {

        if (!consentDetails.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = consentDetails.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);
            if (isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();

                fromTransaction = isNotNull(data.get(TRANSACTION_FROM_DATETIME)) ?
                        Instant.parse(data.get(TRANSACTION_FROM_DATETIME).getAsString()).toDateTime() :
                        null;

                toTransaction = isNotNull(data.get(TRANSACTION_TO_DATETIME)) ?
                        Instant.parse(data.get(TRANSACTION_TO_DATETIME).getAsString()).toDateTime() :
                        null;

                expiredDate = isNotNull(data.get(EXPIRATION_DATETIME)) ?
                        Instant.parse(data.get(EXPIRATION_DATETIME).getAsString()).toDateTime() :
                        null;

                if (isNotNull(data.get(PERMISSIONS))) {
                    setPermissions(data.getAsJsonArray(PERMISSIONS));
                }
            }
        }
    }

    public void setPermissions(JsonArray permissions) {
        if (permissions == null || permissions.size() == 0)
            this.permissions = null;
        else {
            List<FRExternalPermissionsCode> permissionsCodeList = new ArrayList<>();
            for (JsonElement permission : permissions) {
                permissionsCodeList.add(FRExternalPermissionsCode.fromValue(permission.getAsString()));
            }

            this.permissions = permissionsCodeList;
        }
    }
}
