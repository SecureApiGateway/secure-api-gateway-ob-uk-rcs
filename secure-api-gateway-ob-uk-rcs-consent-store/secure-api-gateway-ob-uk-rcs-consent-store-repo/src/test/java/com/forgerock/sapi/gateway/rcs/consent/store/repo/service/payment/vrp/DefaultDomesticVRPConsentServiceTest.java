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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.vrp;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRChargeBearerType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVRPConsentConverters;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.vrp.DomesticVRPConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpConsentRequestTestDataFactory;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DefaultDomesticVRPConsentServiceTest extends BaseConsentServiceTest<DomesticVRPConsentEntity, PaymentAuthoriseConsentArgs> {

    @Autowired
    private DefaultDomesticVRPConsentService service;

    @Override
    protected ConsentStateModel getConsentStateModel() {
        return VRPConsentStateModel.getInstance();
    }

    @Override
    protected BaseConsentService<DomesticVRPConsentEntity, PaymentAuthoriseConsentArgs> getConsentServiceToTest() {
        return service;
    }

    @Override
    protected DomesticVRPConsentEntity getValidConsentEntity() {
        final String apiClientId = "test-client-987";
        return createValidConsentEntity(apiClientId);
    }

    @Override
    protected PaymentAuthoriseConsentArgs getAuthoriseConsentArgs(String consentId, String resourceOwnerId, String apiClientId) {
        return new PaymentAuthoriseConsentArgs(consentId, apiClientId, resourceOwnerId, "debtor-acc-444");
    }

    @Override
    protected void validateConsentSpecificFields(DomesticVRPConsentEntity expected, DomesticVRPConsentEntity actual) {
        assertThat(actual.getIdempotencyKey()).isEqualTo(expected.getIdempotencyKey());
        assertThat(actual.getIdempotencyKeyExpiration()).isEqualTo(expected.getIdempotencyKeyExpiration());
        assertThat(actual.getCharges()).isEqualTo(expected.getCharges());
    }

    @Override
    protected void validateConsentSpecificAuthorisationFields(DomesticVRPConsentEntity authorisedConsent, PaymentAuthoriseConsentArgs authorisationArgs) {
        assertThat(authorisedConsent.getAuthorisedDebtorAccountId()).isEqualTo(authorisationArgs.getAuthorisedDebtorAccountId());
    }

    public static DomesticVRPConsentEntity createValidConsentEntity(String apiClientId) {
        return createValidConsentEntity(OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest(), apiClientId);
    }

    public static DomesticVRPConsentEntity createValidConsentEntity(OBDomesticVRPConsentRequest obConsent, String apiClientId) {
        final DomesticVRPConsentEntity vrpConsent = new DomesticVRPConsentEntity();
        vrpConsent.setRequestVersion(OBVersion.v3_1_10);
        vrpConsent.setApiClientId(apiClientId);
        vrpConsent.setRequestObj(FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(obConsent));
        vrpConsent.setStatus(StatusEnum.AWAITINGAUTHORISATION.toString());
        vrpConsent.setIdempotencyKey(UUID.randomUUID().toString());
        vrpConsent.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        vrpConsent.setCharges(List.of(
                FRCharge.builder().type("fee1")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.15","GBP"))
                        .build(),
                FRCharge.builder().type("fee2")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.10","GBP"))
                        .build())
        );
        return vrpConsent;
    }

    @Test
    void testConsentCanBeReAuthenticated() {
        final DomesticVRPConsentEntity consent = createValidConsentEntity("client-id");
        consent.setStatus(getConsentStateModel().getAuthorisedConsentStatus());
        Assertions.assertThat(consentService.canTransitionToAuthorisedState(consent)).isTrue();
    }

}