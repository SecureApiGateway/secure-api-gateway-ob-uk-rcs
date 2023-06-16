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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentServiceTest;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.common.OBChargeBearerType1Code;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5DataCharges;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class DefaultDomesticPaymentConsentServiceTest extends BaseConsentServiceTest<DomesticPaymentConsentEntity, DomesticPaymentAuthoriseConsentArgs> {

    @Autowired
    private DefaultDomesticPaymentConsentService service;

    @Override
    protected BaseConsentService<DomesticPaymentConsentEntity, DomesticPaymentAuthoriseConsentArgs> getConsentServiceToTest() {
        return service;
    }

    @Override
    protected String getNewConsentStatus() {
        return StatusEnum.AWAITINGAUTHORISATION.toString();
    }

    @Override
    protected String getAuthorisedConsentStatus() {
        return StatusEnum.AUTHORISED.toString();
    }

    @Override
    protected String getRejectedConsentStatus() {
        return StatusEnum.REJECTED.toString();
    }

    @Override
    protected DomesticPaymentAuthoriseConsentArgs getAuthoriseConsentArgs(String consentId, String resourceOwnerId, String apiClientId) {
        return new DomesticPaymentAuthoriseConsentArgs(consentId, apiClientId, resourceOwnerId, "debtor-acc-444");
    }

    @Override
    protected void validateConsentSpecificAuthorisationFields(DomesticPaymentConsentEntity authorisedConsent, DomesticPaymentAuthoriseConsentArgs authorisationArgs) {
        assertThat(authorisedConsent.getAuthorisedDebtorAccountId()).isEqualTo(authorisationArgs.getAuthorisedDebtorAccountId());
    }

    @Override
    protected void validateConsentSpecificFields(DomesticPaymentConsentEntity expected, DomesticPaymentConsentEntity actual) {
        assertThat(actual.getIdempotencyKey()).isEqualTo(expected.getIdempotencyKey());
        assertThat(actual.getIdempotencyKeyExpiration()).isEqualTo(expected.getIdempotencyKeyExpiration());
        assertThat(actual.getCharges()).isEqualTo(expected.getCharges());
    }

    @Override
    protected DomesticPaymentConsentEntity getValidConsentEntity() {
        final OBWriteDomesticConsent4 obConsent = OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4();
        final String apiClientId = "test-client-987";

        final DomesticPaymentConsentEntity domesticPaymentConsent = new DomesticPaymentConsentEntity();
        domesticPaymentConsent.setRequestType(obConsent.getClass().getSimpleName());
        domesticPaymentConsent.setRequestVersion(OBVersion.v3_1_10);
        domesticPaymentConsent.setApiClientId(apiClientId);
        domesticPaymentConsent.setRequestObj(obConsent);
        domesticPaymentConsent.setStatus(StatusEnum.AWAITINGAUTHORISATION.toString());
        domesticPaymentConsent.setIdempotencyKey(UUID.randomUUID().toString());
        domesticPaymentConsent.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        domesticPaymentConsent.setCharges(List.of(new OBWriteDomesticConsentResponse5DataCharges().amount(new OBActiveOrHistoricCurrencyAndAmount().currency("GBP").amount("11.23")).chargeBearer(OBChargeBearerType1Code.BORNEBYCREDITOR).type("fee")));
        return domesticPaymentConsent;
    }

    @Test
    void createConsumeShouldBeIdempotent() {
        final String idempotencyKey = "key-1";
        final DateTime idempotencyKeyExpiry = DateTime.now().plusDays(1);

        DomesticPaymentConsentEntity firstCreateResponse = null;
        for (int i = 0 ; i < 10; i++) {
            final DomesticPaymentConsentEntity validConsentEntity = getValidConsentEntity();
            validConsentEntity.setIdempotencyKey(idempotencyKey);
            validConsentEntity.setIdempotencyKeyExpiration(idempotencyKeyExpiry);
            final DomesticPaymentConsentEntity consentResponse = service.createConsent(validConsentEntity);
            if (firstCreateResponse == null) {
                firstCreateResponse = consentResponse;
            } else {
                assertThat(consentResponse).usingRecursiveComparison().isEqualTo(firstCreateResponse);
            }
        }
    }

    @Test
    void consumeConsent() {
        final DomesticPaymentConsentEntity persistedConsent = service.createConsent(getValidConsentEntity());
        service.authoriseConsent(getAuthoriseConsentArgs(persistedConsent.getId(), TEST_RESOURCE_OWNER, persistedConsent.getApiClientId()));
        final DomesticPaymentConsentEntity consumedConsent = service.consumeConsent(persistedConsent.getId(), persistedConsent.getApiClientId());
        assertThat(consumedConsent.getStatus()).isEqualTo(StatusEnum.CONSUMED.toString());
    }

    @Test
    void failToConsumeConsentAwaitingAuthorisation() {
        final DomesticPaymentConsentEntity persistedConsent = service.createConsent(getValidConsentEntity());
        final ConsentStoreException consentStoreException = Assertions.assertThrows(ConsentStoreException.class, () -> service.consumeConsent(persistedConsent.getId(), persistedConsent.getApiClientId()));
        assertThat(consentStoreException.getConsentId()).isEqualTo(persistedConsent.getId());
        assertThat(consentStoreException.getErrorType()).isEqualTo(ErrorType.INVALID_STATE_TRANSITION);
        assertThat(consentStoreException.getMessage()).contains("cannot transition from consentStatus: AwaitingAuthorisation to status: Consumed");
    }
}