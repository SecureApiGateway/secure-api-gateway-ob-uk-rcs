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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.account;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRExternalPermissionsCodeConverter;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.BaseConsentDetailsService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.account.OBReadData1;

@Component
public class AccountAccessConsentDetailsService extends BaseConsentDetailsService<AccountAccessConsentEntity, AccountsConsentDetails> {

    private final AccountService accountService;

    public AccountAccessConsentDetailsService(ConsentService<AccountAccessConsentEntity, ?> consentService,
            ApiProviderConfiguration apiProviderConfiguration, ApiClientServiceClient apiClientService, AccountService accountService) {

        super(IntentType.ACCOUNT_ACCESS_CONSENT, AccountsConsentDetails::new, consentService,
                apiProviderConfiguration, apiClientService);
        this.accountService = accountService;
    }

    @Override
    protected void addIntentTypeSpecificData(AccountsConsentDetails consentDetails, AccountAccessConsentEntity consent,
                                             ConsentClientDetailsRequest consentClientDetailsRequest) {

        final OBReadData1 readData = consent.getRequestObj().getData();
        consentDetails.setPermissions(FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList(readData.getPermissions()));
        consentDetails.setFromTransaction(readData.getTransactionFromDateTime());
        consentDetails.setToTransaction(readData.getTransactionToDateTime());
        consentDetails.setExpiredDate(readData.getExpirationDateTime());
        consentDetails.setAccounts(accountService.getAccountsWithBalance(consentDetails.getUserId()));
    }
}
