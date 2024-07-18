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
package com.forgerock.sapi.gateway.rcs.consent.store.api.customerinfo.v1_0;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.api.BaseControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.AuthoriseCustomerInfoConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.CreateCustomerInfoConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.customerinfo.v1_0.CustomerInfoConsent;
import uk.org.openbanking.datamodel.v3.account.OBReadConsent1;
import uk.org.openbanking.datamodel.v3.account.OBReadConsent1Data;
import uk.org.openbanking.datamodel.v3.account.OBRisk2;

import jakarta.annotation.PostConstruct;
import uk.org.openbanking.datamodel.v3.common.OBExternalPermissions1Code;

import java.util.List;

public class CustomerInfoConsentApiControllerTest extends BaseControllerTest<CustomerInfoConsent, CreateCustomerInfoConsentRequest, AuthoriseCustomerInfoConsentRequest> {


    protected CustomerInfoConsentApiControllerTest() {
        super(CustomerInfoConsent.class);
    }

    @PostConstruct
    public void postConstruct() {
        apiBaseUrl = "http://localhost:" + port + "/consent/store/v1.0/" + getControllerEndpointName();
    }

    @Override
    protected String getControllerEndpointName() {
        return "customer-info-consents";
    }

    @Override
    protected CreateCustomerInfoConsentRequest buildCreateConsentRequest(String apiClientId) {
        final CreateCustomerInfoConsentRequest createRequest = new CreateCustomerInfoConsentRequest();
        createRequest.setApiClientId(apiClientId);
        createRequest.setConsentRequest(FRReadConsentConverter.toFRReadConsent(new OBReadConsent1()
                .data(new OBReadConsent1Data().permissions(List.of(OBExternalPermissions1Code.READCUSTOMERINFO)))
                .risk(new OBRisk2())));
        return createRequest;
    }

    @Override
    protected void validateCreateConsentAgainstCreateRequest(CustomerInfoConsent consent, CreateCustomerInfoConsentRequest createConsentRequest) {
        CustomerInfoConsentValidationHelpers.validateCreateConsentAgainstCreateRequest(consent, createConsentRequest);
    }

    @Override
    protected AuthoriseCustomerInfoConsentRequest buildAuthoriseConsentRequest(CustomerInfoConsent consent, String resourceOwnerId) {
        AuthoriseCustomerInfoConsentRequest authoriseCustomerInfoConsentRequest = new AuthoriseCustomerInfoConsentRequest();
        authoriseCustomerInfoConsentRequest.setConsentId(consent.getId());
        authoriseCustomerInfoConsentRequest.setApiClientId(consent.getApiClientId());
        authoriseCustomerInfoConsentRequest.setResourceOwnerId(resourceOwnerId);
        return authoriseCustomerInfoConsentRequest;
    }

    @Override
    protected void validateAuthorisedConsent(CustomerInfoConsent authorisedConsent, AuthoriseCustomerInfoConsentRequest authoriseConsentReq, CustomerInfoConsent originalConsent) {
        CustomerInfoConsentValidationHelpers.validateAuthorisedConsent(authorisedConsent, authoriseConsentReq, originalConsent);
    }

    @Override
    protected void validateRejectedConsent(CustomerInfoConsent rejectedConsent, RejectConsentRequest rejectConsentRequest, CustomerInfoConsent originalConsent) {
        CustomerInfoConsentValidationHelpers.validateRejectedConsent(rejectedConsent, rejectConsentRequest, originalConsent);
    }
}
