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
package com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

/**
 * Models the consent data that is used for an account details request.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AccountsConsentDetails extends ConsentDetails {

    private List<FRExternalPermissionsCode> permissions;
    private List<FRAccountWithBalance> accounts;
    private DateTime fromTransaction;
    private DateTime toTransaction;
    private String aispName;
    private DateTime expiredDate;

    @Override
    public IntentType getIntentType() {
        return IntentType.ACCOUNT_ACCESS_CONSENT;
    }

    public void setPermissions(JsonArray permissions) {
        List<FRExternalPermissionsCode> permissionsCodeList = Collections.emptyList();
        for(JsonElement permission: permissions)
        {
            permissionsCodeList.add(FRExternalPermissionsCode.fromValue(permission.getAsString()));
        }

        this.permissions = permissionsCodeList;
    }

    public void setFromTransaction(String fromTransaction) {
        this.fromTransaction = new DateTime(fromTransaction);
    }

    public void setToTransaction(String toTransaction) {
        this.toTransaction = new DateTime(toTransaction);
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = new DateTime(expiredDate);
    }
}
