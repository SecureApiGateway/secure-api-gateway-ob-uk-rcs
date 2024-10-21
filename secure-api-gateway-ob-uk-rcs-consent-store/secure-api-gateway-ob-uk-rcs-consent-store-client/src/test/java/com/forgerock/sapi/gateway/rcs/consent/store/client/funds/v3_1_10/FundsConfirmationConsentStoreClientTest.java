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
package com.forgerock.sapi.gateway.rcs.consent.store.client.funds.v3_1_10;

import static com.forgerock.sapi.gateway.rcs.consent.store.api.funds.FundsConfirmationConsentValidationHelpers.validateAuthorisedConsent;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.funds.FundsConfirmationConsentValidationHelpers.validateCreateConsentAgainstCreateRequest;
import static com.forgerock.sapi.gateway.rcs.consent.store.api.funds.FundsConfirmationConsentValidationHelpers.validateRejectedConsent;
import static com.forgerock.sapi.gateway.rcs.consent.store.client.TestConsentStoreClientConfigurationFactory.createConsentStoreClientConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.UUID;

import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.BaseFundsConfirmationConsentStoreClientTest;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.BaseRestFundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.FundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticstandingorder.BaseRestDomesticStandingOrderConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticstandingorder.v3_1_10.RestDomesticStandingOrderConsentStoreClient;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.funds.FRFundsConfirmationConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.AuthoriseFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.CreateFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;

import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsent1;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsent1Data;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsent1DataDebtorAccount;

/**
 * Test for {@link FundsConfirmationConsentStoreClient}
 */

class FundsConfirmationConsentStoreClientTest extends BaseFundsConfirmationConsentStoreClientTest {

    protected FundsConfirmationConsentStoreClientTest() {
        super(OBVersion.v3_1_10);
    }

    @Override
    protected BaseRestFundsConfirmationConsentStoreClient createApiClient() {
        return new RestFundsConfirmationConsentStoreClient(createConsentStoreClientConfiguration(port),
                restTemplateBuilder,
                objectMapper);
    }

}
