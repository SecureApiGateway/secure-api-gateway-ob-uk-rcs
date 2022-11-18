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

/**
 * Class of constants to resolve consent json-based members
 */
public class ConsentDetailsConstants {

    public static final class intent {
        public static final String CONSENT_ID = "ConsentId";
        public static final String OB_INTENT_OBJECT = "OBIntentObject";
        public static final String OAUTH2_CLIENT_NAME = "oauth2ClientName";

        public static final class members {
            public static final String DATA = "Data";
            public static final String EXCHANGE_RATE = "ExchangeRate";
            public static final String RATE_TYPE = "RateType";
            public static final String EXCHANGE_RATE_INFORMATION = "ExchangeRateInformation";
            public static final String CONTRACT_IDENTIFICATION = "ContractIdentification";
            public static final String TRANSACTION_FROM_DATETIME = "TransactionFromDateTime";
            public static final String TRANSACTION_TO_DATETIME = "TransactionToDateTime";
            public static final String EXPIRATION_DATETIME = "ExpirationDateTime";
            public static final String FINAL_PAYMENT_DATETIME = "FinalPaymentDateTime";
            public static final String FINAL_PAYMENT_AMOUNT = "FinalPaymentAmount";
            public static final String FIRST_PAYMENT_DATETIME = "FirstPaymentDateTime";
            public static final String FIRST_PAYMENT_AMOUNT = "FirstPaymentAmount";
            public static final String RECURRING_PAYMENT_DATETIME = "RecurringPaymentDateTime";
            public static final String RECURRING_PAYMENT_AMOUNT = "RecurringPaymentAmount";
            public static final String FREQUENCY = "Frequency";
            public static final String PERMISSIONS = "Permissions";
            public static final String INITIATION = "Initiation";
            public static final String NUMBER_OF_TRANSACTIONS = "NumberOfTransactions";
            public static final String CONTROL_SUM = "ControlSum";
            public static final String FILE_REFERENCE = "FileReference";
            public static final String REQUESTED_EXECUTION_DATETIME = "RequestedExecutionDateTime";
            public static final String INSTRUCTED_AMOUNT = "InstructedAmount";
            public static final String REMITTANCE_INFORMATION = "RemittanceInformation";
            public static final String REFERENCE = "Reference";
            public static final String CURRENCY_OF_TRANSFER = "CurrencyOfTransfer";
            public static final String CHARGES = "Charges";
            public static final String DEBTOR_ACCOUNT = "DebtorAccount";
            public static final String CREDITOR_ACCOUNT = "CreditorAccount";
            public static final String CONTROL_PARAMETERS = "ControlParameters";
            public static final String VRP_TYPE = "VRPType";
            public static final String PSU_AUTHENTICATION_METHODS = "PSUAuthenticationMethods";
            public static final String VALID_FROM_DATETIME = "ValidFromDateTime";
            public static final String VALID_TO_DATETIME = "ValidToDateTime";
            public static final String MAXIMUM_INDIVIDUAL_AMOUNT = "MaximumIndividualAmount";
            public static final String AMOUNT = "Amount";
            public static final String CURRENCY = "Currency";
            public static final String UNIT_CURRENCY = "UnitCurrency";
            public static final String PERIODIC_LIMITS = "PeriodicLimits";
            public static final String PERIOD_ALIGNMENT = "PeriodAlignment";
            public static final String PERIOD_TYPE = "PeriodType";
            public static final String SCHEME_NAME = "SchemeName";
            public static final String NAME = "Name";
            public static final String IDENTIFICATION = "Identification";
            public static final String SECONDARY_IDENTIFICATION = "SecondaryIdentification";
            public static final String UNSTRUCTURED = "Unstructured";
        }
    }
}
