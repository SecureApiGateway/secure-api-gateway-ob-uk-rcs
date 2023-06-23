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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.PaymentsConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.api.details.BaseConsentDetailsService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.BasePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.google.common.annotations.VisibleForTesting;

public abstract class BasePaymentConsentDetailsService<T extends BasePaymentConsentEntity, D extends PaymentsConsentDetails> extends BaseConsentDetailsService<T, D>  {

    protected final AccountService accountService;

    public BasePaymentConsentDetailsService(IntentType supportedIntentType, Supplier<D> consentDetailsObjSupplier,
                                            ConsentService<T, ?> consentService, ApiProviderConfiguration apiProviderConfiguration,
                                            ApiClientServiceClient apiClientService, AccountService accountService) {
        super(supportedIntentType, consentDetailsObjSupplier, consentService, apiProviderConfiguration, apiClientService);
        this.accountService = accountService;
    }

    @VisibleForTesting
    static FRAmount computeTotalChargeAmount(List<FRCharge> charges) {
        String chargeCurrency = null;
        BigDecimal totalCharge = BigDecimal.ZERO;
        for (FRCharge charge : charges) {
            final FRAmount amount = charge.getAmount();
            if (chargeCurrency == null) {
                chargeCurrency = amount.getCurrency();
            } else if (!chargeCurrency.equals(amount.getCurrency())) {
                throw new IllegalStateException("Charges contain more than 1 currency, all charges must be in the same currency");
            }
            totalCharge = totalCharge.add(new BigDecimal(amount.getAmount()));
        }
        return new FRAmount(totalCharge.toPlainString(), chargeCurrency);
    }

    protected void addDebtorAccountDetails(D consentDetails) {
        final FRAccountIdentifier debtorAccount = consentDetails.getDebtorAccount();
        if (Objects.nonNull(debtorAccount)) {
            FRAccountWithBalance accountWithBalance = accountService.getAccountWithBalanceByIdentifiers(
                    consentDetails.getUserId(), debtorAccount.getName(), debtorAccount.getIdentification(), debtorAccount.getSchemeName());

            if (Objects.nonNull(accountWithBalance)) {
                debtorAccount.setAccountId(accountWithBalance.getAccount().getAccountId());
                consentDetails.setAccounts(List.of(accountWithBalance));
            } else {
                logger.warn("Failed to set debtorAccount details for consentId: {}, " +
                                "no account found for userId: {}, name:{}, identification: {}, schemeName: {}",
                        consentDetails.getConsentId(), consentDetails.getUserId(), debtorAccount.getName(),
                        debtorAccount.getIdentification(), debtorAccount.getSchemeName());
                throw new ConsentStoreException(ErrorType.NOT_FOUND, consentDetails.getConsentId(), "DebtorAccount not found for user");
            }

        } else {
            consentDetails.setAccounts(accountService.getAccountsWithBalance(consentDetails.getUserId()));
        }
    }
}
