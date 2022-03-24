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
package com.forgerock.securebanking.openbanking.uk.rcs;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticPaymentsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.accounts.AccountConsentDetailsConverter;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.domestic.payments.DomesticPaymentConsentDetailsConverter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RcsApplicationTestSupport {

    public static void main(String[] args) {
        String responseEntity = "{\"id\":\"PDC_6c563ae6-6c06-49f6-82d1-1e7be499ceb8\",\"data\":{\"Initiation\":{\"InstructionIdentification\":\"ACME412\",\"EndToEndIdentification\":\"FRESCO.21302.GFX.20\",\"InstructedAmount\":{\"Amount\":\"165.88\",\"Currency\":\"GBP\"},\"CreditorAccount\":{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"08080021325698\",\"Name\":\"ACME Inc\",\"SecondaryIdentification\":\"0002\"},\"RemittanceInformation\":{\"Reference\":\"FRESCO-101\",\"Unstructured\":\"Internal ops code 5120101\"}},\"ConsentId\":\"PDC_6c563ae6-6c06-49f6-82d1-1e7be499ceb8\",\"Status\":\"AwaitingAuthorisation\",\"CreationDateTime\":\"2022-03-22T12:32:50.089Z\",\"StatusUpdateDateTime\":\"2022-03-22T12:32:50.089Z\"},\"resourceOwnerUsername\":null,\"oauth2ClientId\":\"bd04b4c3-79d1-41b9-ab7e-1048cde88367\",\"oauth2ClientName\":\"Automating-testing\"}";
        JsonObject IDM_rep = new JsonParser().parse(responseEntity).getAsJsonObject();
        System.out.println(IDM_rep);
        DomesticPaymentConsentDetailsConverter consentDetailsConverter = DomesticPaymentConsentDetailsConverter.getInstance();
        DomesticPaymentsConsentDetails details = consentDetailsConverter.toDomesticPaymentConsentDetails(IDM_rep);

        String responseEntityAccount = "{\"id\":\"PDC_6c563ae6-6c06-49f6-82d1-1e7be499ceb8\",\"data\":{\"Initiation\":{\"InstructionIdentification\":\"ACME412\",\"EndToEndIdentification\":\"FRESCO.21302.GFX.20\",\"InstructedAmount\":{\"Amount\":\"165.88\",\"Currency\":\"GBP\"},\"CreditorAccount\":{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"08080021325698\",\"Name\":\"ACME Inc\",\"SecondaryIdentification\":\"0002\"},\"RemittanceInformation\":{\"Reference\":\"FRESCO-101\",\"Unstructured\":\"Internal ops code 5120101\"}},\"ConsentId\":\"PDC_6c563ae6-6c06-49f6-82d1-1e7be499ceb8\",\"Status\":\"AwaitingAuthorisation\",\"CreationDateTime\":\"2022-03-22T12:32:50.089Z\",\"StatusUpdateDateTime\":\"2022-03-22T12:32:50.089Z\"},\"resourceOwnerUsername\":null,\"oauth2ClientId\":\"bd04b4c3-79d1-41b9-ab7e-1048cde88367\",\"oauth2ClientName\":\"Automating-testing\"}";
        JsonObject IDM_rep_account = new JsonParser().parse(responseEntity).getAsJsonObject();
        System.out.println(IDM_rep_account);
        AccountConsentDetailsConverter accountConsentDetailsConverter = AccountConsentDetailsConverter.getInstance();
        AccountsConsentDetails accountsConsentDetails = accountConsentDetailsConverter.toAccountConsentDetails(IDM_rep_account);


    }
}
