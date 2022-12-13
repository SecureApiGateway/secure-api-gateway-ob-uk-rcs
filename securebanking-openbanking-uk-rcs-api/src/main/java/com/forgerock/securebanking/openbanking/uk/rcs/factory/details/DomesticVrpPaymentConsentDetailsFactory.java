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
package com.forgerock.securebanking.openbanking.uk.rcs.factory.details;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRAccountIdentifier;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRRemittanceInformation;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.vrp.FRWriteDomesticVrpDataInitiation;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticVrpPaymentConsentDetails;
import com.forgerock.securebanking.platform.client.IntentType;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Instant;
import org.springframework.stereotype.Component;
import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParameters;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParametersPeriodicLimits;

import java.util.ArrayList;
import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.Members.*;
import static com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.ConsentDetailsConstants.Intent.OB_INTENT_OBJECT;
import static com.forgerock.securebanking.openbanking.uk.rcs.json.utils.JsonUtilValidation.isNotNull;
import static java.util.Objects.requireNonNull;

/**
 * Domestic Vrp Payment consent details factory implements {@link ConsentDetailsFactory}
 */
@Slf4j
@Component
public class DomesticVrpPaymentConsentDetailsFactory implements ConsentDetailsFactory<DomesticVrpPaymentConsentDetails> {

    @Override
    public DomesticVrpPaymentConsentDetails decode(JsonObject json) {
        requireNonNull(json, "decode(json) parameter 'json' cannot be null");
        DomesticVrpPaymentConsentDetails details = new DomesticVrpPaymentConsentDetails();
        if (!json.has(OB_INTENT_OBJECT)) {
            throw new IllegalStateException("Expected " + OB_INTENT_OBJECT + " field in json");
        } else {
            final JsonObject obIntentObject = json.get(OB_INTENT_OBJECT).getAsJsonObject();
            final JsonElement consentDataElement = obIntentObject.get(DATA);

            if (isNotNull(consentDataElement)) {
                JsonObject data = consentDataElement.getAsJsonObject();
                log.debug("{}.{}: {}", OB_INTENT_OBJECT, DATA, data);

                if (isNotNull(data.get(INITIATION))) {
                    details.setInitiation(decodeDataInitiation(data));
                }

                if (isNotNull(data.get(CONTROL_PARAMETERS))) {
                    details.setControlParameters(decodeControlParameters(data));
                }

            }
        }
        return details;
    }

    @Override
    public IntentType getIntentType() {
        return IntentType.DOMESTIC_VRP_PAYMENT_CONSENT;
    }

    private FRWriteDomesticVrpDataInitiation decodeDataInitiation(JsonObject data) {
        JsonObject initiation = data.getAsJsonObject(INITIATION);
        log.debug("{}.{}.{}: {}", OB_INTENT_OBJECT, DATA, INITIATION, initiation);
        JsonObject debtorAccount = initiation.getAsJsonObject(DEBTOR_ACCOUNT);
        JsonObject creditorAccount = initiation.getAsJsonObject(CREDITOR_ACCOUNT);
        FRWriteDomesticVrpDataInitiation vrpDataInitiation = FRWriteDomesticVrpDataInitiation.builder()
                .creditorAccount(
                        FRAccountIdentifier.builder()
                                .identification(creditorAccount.get(IDENTIFICATION).getAsString())
                                .name(creditorAccount.get(NAME).getAsString())
                                .schemeName(creditorAccount.get(SCHEME_NAME).getAsString())
                                .secondaryIdentification(
                                        isNotNull(creditorAccount.get(SECONDARY_IDENTIFICATION)) ?
                                                creditorAccount.get(SECONDARY_IDENTIFICATION).getAsString() :
                                                null
                                )
                                .build()
                )
                .debtorAccount(
                        FRAccountIdentifier.builder()
                                .identification(debtorAccount.get(IDENTIFICATION).getAsString())
                                .name(debtorAccount.get(NAME).getAsString())
                                .schemeName(debtorAccount.get(SCHEME_NAME).getAsString())
                                .secondaryIdentification(
                                        isNotNull(debtorAccount.get(SECONDARY_IDENTIFICATION)) ?
                                                debtorAccount.get(SECONDARY_IDENTIFICATION).getAsString() :
                                                null
                                )
                                .build()
                )
                .build();

        if (isNotNull(initiation.get(REMITTANCE_INFORMATION))) {
            JsonObject remittanceInformation = initiation.getAsJsonObject(REMITTANCE_INFORMATION);
            vrpDataInitiation.setRemittanceInformation(
                    FRRemittanceInformation.builder()
                            .unstructured(
                                    isNotNull(remittanceInformation.get(UNSTRUCTURED)) ?
                                            remittanceInformation.get(UNSTRUCTURED).getAsString() :
                                            null
                            )
                            .reference(
                                    isNotNull(remittanceInformation.get(REFERENCE)) ?
                                            remittanceInformation.get(REFERENCE).getAsString() :
                                            null
                            )
                            .build()
            );
        }
        return vrpDataInitiation;
    }

    private OBDomesticVRPControlParameters decodeControlParameters(JsonObject data) {
        final JsonObject controlParameters = data.get(CONTROL_PARAMETERS).getAsJsonObject();
        log.debug("{}.{}.{}: {}", OB_INTENT_OBJECT, DATA, CONTROL_PARAMETERS, controlParameters);

        OBDomesticVRPControlParameters ctrlParams = new OBDomesticVRPControlParameters();

        if (isNotNull(controlParameters.get(VALID_FROM_DATETIME))) {
            ctrlParams.setValidFromDateTime(Instant.parse(controlParameters.get(VALID_FROM_DATETIME).getAsString()).toDateTime());
        }

        if (isNotNull(controlParameters.get(VALID_TO_DATETIME))) {
            ctrlParams.setValidToDateTime(Instant.parse(controlParameters.get(VALID_TO_DATETIME).getAsString()).toDateTime());
        }

        List<String> vrpTypeList = new Gson().fromJson(
                controlParameters.get(VRP_TYPE), new TypeToken<List<String>>() {
                }.getType()
        );
        ctrlParams.setVrPType(vrpTypeList);
        List<String> vrpAuthMethodsList = new Gson().fromJson(
                controlParameters.get(PSU_AUTHENTICATION_METHODS), new TypeToken<List<String>>() {
                }.getType()
        );
        ctrlParams.setPsUAuthenticationMethods(vrpAuthMethodsList);

        if (isNotNull(controlParameters.get(MAXIMUM_INDIVIDUAL_AMOUNT))) {
            ctrlParams.setMaximumIndividualAmount(decodeMaximumIndividualAmount(controlParameters));
        }

        if (isNotNull(controlParameters.get(PERIODIC_LIMITS))) {
            ctrlParams.setPeriodicLimits(decodePeriodicLimits(controlParameters));
        }
        return ctrlParams;
    }

    private OBActiveOrHistoricCurrencyAndAmount decodeMaximumIndividualAmount(JsonObject controlParameters) {
        final JsonObject maximumIndividualAmount = controlParameters.get(MAXIMUM_INDIVIDUAL_AMOUNT).getAsJsonObject();
        log.debug("{}.{}.{}.{}: {}", OB_INTENT_OBJECT, DATA, CONTROL_PARAMETERS, MAXIMUM_INDIVIDUAL_AMOUNT, maximumIndividualAmount);
        OBActiveOrHistoricCurrencyAndAmount maxIndividualAmount = new OBActiveOrHistoricCurrencyAndAmount();
        maxIndividualAmount.setAmount(maximumIndividualAmount.get(AMOUNT).getAsString());
        maxIndividualAmount.setCurrency(maximumIndividualAmount.get(CURRENCY).getAsString());
        return maxIndividualAmount;
    }

    private List<OBDomesticVRPControlParametersPeriodicLimits> decodePeriodicLimits(JsonObject controlParameters) {
        final JsonArray periodicLimits = controlParameters.get(PERIODIC_LIMITS).getAsJsonArray();
        log.debug("{}.{}.{}.{}: {}", OB_INTENT_OBJECT, DATA, CONTROL_PARAMETERS, PERIODIC_LIMITS, periodicLimits);
        List<OBDomesticVRPControlParametersPeriodicLimits> periodicLimitsList = new ArrayList<>();
        for (JsonElement periodicLimit : periodicLimits) {
            JsonObject periodicLimitObj = periodicLimit.getAsJsonObject();
            if (periodicLimitObj != null) {
                OBDomesticVRPControlParametersPeriodicLimits periodicLimitElement = new OBDomesticVRPControlParametersPeriodicLimits();
                periodicLimitElement.setAmount(periodicLimitObj.get(AMOUNT).getAsString());
                periodicLimitElement.setCurrency(periodicLimitObj.get(CURRENCY).getAsString());
                periodicLimitElement.setPeriodAlignment(
                        OBDomesticVRPControlParametersPeriodicLimits.PeriodAlignmentEnum.fromValue(
                                periodicLimitObj.get(PERIOD_ALIGNMENT).getAsString()
                        )
                );
                periodicLimitElement.setPeriodType(
                        OBDomesticVRPControlParametersPeriodicLimits.PeriodTypeEnum.fromValue(
                                periodicLimitObj.get(PERIOD_TYPE).getAsString()
                        )
                );
                periodicLimitsList.add(periodicLimitElement);
            }
        }
        return periodicLimitsList;
    }
}
