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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAmount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRReadRefundAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRRemittanceInformation;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.vrp.FRWriteDomesticVrpDataInitiation;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.DomesticVrpPaymentConsentDetailsConverter;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAccountIdentifier;
import com.forgerock.securebanking.platform.client.IntentType;
import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joda.time.DateTime;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParametersPeriodicLimits;

import java.math.BigDecimal;
import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rcs.converters.DomesticVrpPaymentConsentDetailsConverter.DATE_TIME_FORMATTER;
import static com.forgerock.securebanking.openbanking.uk.rcs.converters.UtilConverter.isNotNull;

/**
 * Models the consent data for a domestic vrp payment.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class DomesticVrpPaymentConsentDetails extends ConsentDetails {

    private FRWriteDomesticVrpDataInitiation domesticVrpPayment;
    private List<FRAccountWithBalance> accounts;
    private FRAmount charges;
    private String merchantName;
    private String pispName;
    private DateTime expiredDate;
    private String currencyOfTransfer;
    private String paymentReference;
    private FRAccountIdentifier debtorAccount;
    private FRAccountIdentifier creditorAccount;
    private FRRemittanceInformation remittanceInformation;
    private String secondaryIdentification;
    private FRReadRefundAccount readRefundAccount;
    private OBDomesticVRPControlParameters obDomesticVRPControlParameters;
    private OBActiveOrHistoricCurrencyAndAmount maximumIndividualAmount;
    private List<OBDomesticVRPControlParametersPeriodicLimits> periodicLimits;
    private OBDomesticVRPControlParameters supplementaryData;

    public void setDomesticVrpPayment(FRWriteDomesticVrpDataInitiation domesticVrpPayment) {
        this.domesticVrpPayment = domesticVrpPayment;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.DOMESTIC_VRP_PAYMENT_CONSENT;
    }

    public void setCharges(JsonArray charges) {
        if (!isNotNull(charges)) {
            this.charges = null;
        } else {
            this.charges = new FRAmount();
            Double amount = 0.0;

            for (JsonElement charge : charges) {
                JsonObject chargeAmount = charge.getAsJsonObject().getAsJsonObject("Amount");
                amount += chargeAmount.get("Amount").getAsDouble();
            }

            String currency = charges.get(0).getAsJsonObject().getAsJsonObject("Amount").get("Currency").getAsString();

            this.charges.setAmount(amount.toString());
            this.charges.setCurrency(currency);
        }
    }

    public void setDomesticVrpPayment() {
        FRWriteDomesticVrpDataInitiation domesticVrpPaymentData = new FRWriteDomesticVrpDataInitiation();

        /*if (isNotNull(secondaryIdentification)) {
            domesticVrpPaymentData.setDebtorAccount(secondaryIdentification.getAsString());
        }

        if (isNotNull(paymentReference)) {
            domesticVrpPaymentData.setRemittanceInformation(paymentReference.getAsBigDecimal());
        }

        if (isNotNull(obDomesticVRPControlParameters)) {
            domesticVrpPaymentData.setDom(obDomesticVRPControlParameters.getAs());
        }*/

        this.domesticVrpPayment = domesticVrpPaymentData;
    }

}

