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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.customerinfo;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.customerinfo.CustomerInfoConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentServiceTest;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.ConsentStateModel;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.org.openbanking.datamodel.account.OBReadConsent1;
import uk.org.openbanking.datamodel.account.OBReadConsent1Data;
import uk.org.openbanking.datamodel.account.OBRisk2;
import uk.org.openbanking.datamodel.common.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link DefaultCustomerInfoAccessConsentService}
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DefaultCustomerInfoAccessConsentServiceTest extends BaseConsentServiceTest<CustomerInfoConsentEntity, CustomerInfoAuthoriseConsentArgs> {
    private static final String API_CLIENT_ID = UUID.randomUUID().toString();

    @Autowired
    private DefaultCustomerInfoAccessConsentService customerInfoAccessConsentService;

    @Override
    protected BaseConsentService<CustomerInfoConsentEntity, CustomerInfoAuthoriseConsentArgs> getConsentServiceToTest() {
        return customerInfoAccessConsentService;
    }

    @Override
    protected ConsentStateModel getConsentStateModel() {
        return CustomerInfoConsentStateModel.getInstance();
    }

    @Override
    protected CustomerInfoConsentEntity getValidConsentEntity() {
        return createValidConsentEntity(API_CLIENT_ID);
    }

    @Override
    protected CustomerInfoAuthoriseConsentArgs getAuthoriseConsentArgs(String consentId, String resourceOwnerId, String apiClientId) {
        return new CustomerInfoAuthoriseConsentArgs(consentId, apiClientId, resourceOwnerId);
    }

    @Override
    protected void validateConsentSpecificFields(CustomerInfoConsentEntity expected, CustomerInfoConsentEntity actual) {
    }

    @Override
    protected void validateConsentSpecificAuthorisationFields(CustomerInfoConsentEntity authorisedConsent, CustomerInfoAuthoriseConsentArgs authorisationArgs) {
    }

    @Test
    void deleteConsent() {
        final CustomerInfoConsentEntity consentObj = getValidConsentEntity();

        final CustomerInfoConsentEntity persistedConsent = consentService.createConsent(consentObj);

        final String consentId = persistedConsent.getId();
        final CustomerInfoAuthoriseConsentArgs authoriseConsentArgs = getAuthoriseConsentArgs(consentId, TEST_RESOURCE_OWNER, consentObj.getApiClientId());
        consentService.authoriseConsent(authoriseConsentArgs);

        consentService.deleteConsent(consentId, consentObj.getApiClientId());
        final ConsentStoreException consentStoreException = Assertions.assertThrows(ConsentStoreException.class, () -> consentService.getConsent(consentId, consentObj.getApiClientId()));
        assertThat(consentStoreException.getErrorType()).isEqualTo(ConsentStoreException.ErrorType.NOT_FOUND);
    }

    @Test
    void testConsentCanBeReAuthenticated() {
        final CustomerInfoConsentEntity consent = createValidConsentEntity(API_CLIENT_ID);
        consent.setStatus(getConsentStateModel().getAuthorisedConsentStatus());
        assertThat(consentService.canTransitionToAuthorisedState(consent)).isTrue();
    }

    public static CustomerInfoConsentEntity createValidConsentEntity(String apiClientId) {
        final CustomerInfoConsentEntity customerInfoConsentEntity = new CustomerInfoConsentEntity();
        customerInfoConsentEntity.setApiClientId(apiClientId);
        customerInfoConsentEntity.setStatus(OBExternalRequestStatus1Code.AWAITINGAUTHORISATION.toString());
        customerInfoConsentEntity.setRequestVersion(OBVersion.v1_0);

        final OBReadConsent1 obReadConsent = new OBReadConsent1();
        obReadConsent.setData(new OBReadConsent1Data().permissions(List.of(OBExternalPermissions1Code.READCUSTOMERINFO))
                .expirationDateTime(DateTime.now().plusDays(30)));
        obReadConsent.setRisk(new OBRisk2());
        customerInfoConsentEntity.setRequestObj(FRReadConsentConverter.toFRReadConsent(obReadConsent));

        return customerInfoConsentEntity;
    }
}
