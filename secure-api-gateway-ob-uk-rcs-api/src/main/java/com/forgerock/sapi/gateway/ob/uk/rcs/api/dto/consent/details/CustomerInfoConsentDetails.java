/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.org.openbanking.datamodel.v3.customerinfo.CustomerInfo;

import java.util.List;

/**
 * Models the customer info data that is used for an customer info details request.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInfoConsentDetails extends ConsentDetails {

    private List<FRExternalPermissionsCode> permissions;
    private FRCustomerInfo customerInfo;

    @Override
    public IntentType getIntentType() {
        return IntentType.CUSTOMER_INFO_CONSENT;
    }
}
