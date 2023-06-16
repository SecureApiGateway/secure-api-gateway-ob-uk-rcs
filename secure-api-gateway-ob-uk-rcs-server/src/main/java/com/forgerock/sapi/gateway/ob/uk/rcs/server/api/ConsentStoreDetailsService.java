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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.ConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.DomesticPaymentConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.details.PaymentsConsentDetails;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ApiClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.models.ConsentClientDetailsRequest;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.services.ApiClientServiceClient;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.client.rs.AccountService;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.configuration.ApiProviderConfiguration;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreEnabledIntentTypes;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4Data;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5DataCharges;

@Component
public class ConsentStoreDetailsService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes;

    private final DomesticPaymentConsentService domesticPaymentConsentService;

    private final AccountService accountService;

    private final ApiClientServiceClient apiClientService;

    private final ApiProviderConfiguration apiProviderConfiguration;

    private final DebtorAccountService debtorAccountService;

    public ConsentStoreDetailsService(ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes, DomesticPaymentConsentService domesticPaymentConsentService,
                                      AccountService accountService, ApiClientServiceClient apiClientService,
                                      ApiProviderConfiguration apiProviderConfiguration, DebtorAccountService debtorAccountService) {
        this.consentStoreEnabledIntentTypes = consentStoreEnabledIntentTypes;
        this.domesticPaymentConsentService = domesticPaymentConsentService;
        this.accountService = accountService;
        this.apiClientService = apiClientService;
        this.apiProviderConfiguration = apiProviderConfiguration;
        this.debtorAccountService = debtorAccountService;
    }

    public boolean isIntentTypeSupported(IntentType intentType) {
        return consentStoreEnabledIntentTypes.isIntentTypeSupported(intentType);
    }

    public ConsentDetails getDetailsFromConsentStore(IntentType intentType, String intentId, ConsentClientDetailsRequest consentClientRequest) throws ExceptionClient {
        if (!isIntentTypeSupported(intentType)) {
            throw new IllegalStateException(intentType + " support not currently implemented in Consent Store module");
        }
        // TODO dispatch based on IntentType
        return getDomesticPaymentConsentDetails(intentId, consentClientRequest);
    }

    private DomesticPaymentConsentDetails getDomesticPaymentConsentDetails(String intentId, ConsentClientDetailsRequest consentClientRequest) throws ExceptionClient {
        final String clientId = consentClientRequest.getClientId();
        logger.info("Fetching Data from RCS Consent Service - consentId: {}, clientId: {}");
        final DomesticPaymentConsentEntity consent = domesticPaymentConsentService.getConsent(intentId, clientId);
        logger.info("Got consent: {}", consent);

        DomesticPaymentConsentDetails details = new DomesticPaymentConsentDetails();
        details.setConsentId(consent.getId());
        details.setUsername(consentClientRequest.getUser().getUserName());
        details.setUserId(consentClientRequest.getUser().getId());
        details.setClientId(clientId);
        details.setServiceProviderName(apiProviderConfiguration.getName());

        // TODO create function that converts List<OBActiveOrHistoricCurrencyAndAmount> into a single FRAmount
        final List<OBWriteDomesticConsentResponse5DataCharges> charges = consent.getCharges();
        String chargeCurrency = null;
        BigDecimal totalCharge = BigDecimal.ZERO;
        for (OBWriteDomesticConsentResponse5DataCharges charge : charges) {
            final OBActiveOrHistoricCurrencyAndAmount amount = charge.getAmount();
            if (chargeCurrency == null) {
                chargeCurrency = amount.getCurrency();
            } else if (!chargeCurrency.equals(amount.getCurrency())) {
                throw new IllegalStateException("Charges for consent: " + consent.getId() + " contain more than 1 currency, all charges must be in the same currency");
            }
            totalCharge.add(new BigDecimal(amount.getAmount()));
        }
        details.setCharges(new FRAmount(totalCharge.toPlainString(), chargeCurrency));

        final OBWriteDomesticConsent4Data obConsentRequestData = consent.getRequestObj().getData();
        final FRWriteDomesticDataInitiation initiation = FRWriteDomesticConsentConverter.toFRWriteDomesticDataInitiation(obConsentRequestData.getInitiation());
        details.setInitiation(initiation);
        details.setInstructedAmount(initiation.getInstructedAmount());

        if ((details instanceof PaymentsConsentDetails) && Objects.nonNull(((PaymentsConsentDetails) details).getDebtorAccount())) {
            debtorAccountService.setDebtorAccountWithBalance(details, consentClientRequest.getConsentRequestJwtString(), intentId);
        } else {
            details.setAccounts(accountService.getAccountsWithBalance(details.getUserId()));
        }

        ApiClient apiClient = apiClientService.getApiClient(consentClientRequest.getClientId());
        logger.debug("ApiClient controller: " + apiClient);
        details.setLogo(apiClient.getLogoUri());
        details.setClientName(apiClient.getName());

        logger.info("Built consentDetails: {}", details);
        return details;
    }
}
