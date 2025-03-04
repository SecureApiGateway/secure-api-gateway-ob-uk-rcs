/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rcs.consent.store.client.payment.vrp;

import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.CreateDomesticVRPConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.UpdateDomesticVRPConsentRequest;

/**
 * Client for interacting with com.forgerock.sapi.gateway.rcs.consent.store.api.v3_1_10.DomesticVRPConsentApi
 */
public interface DomesticVRPConsentStoreClient {

    DomesticVRPConsent createConsent(CreateDomesticVRPConsentRequest createConsentRequest) throws ConsentStoreClientException;

    DomesticVRPConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException;

    DomesticVRPConsent authoriseConsent(AuthorisePaymentConsentRequest authorisePaymentConsentRequest) throws ConsentStoreClientException;

    DomesticVRPConsent rejectConsent(RejectConsentRequest rejectDomesticVRPConsentRequest) throws ConsentStoreClientException;

    void deleteConsent(String consentId, String apiClientId) throws ConsentStoreClientException;

    DomesticVRPConsent update1Consent(UpdateDomesticVRPConsentRequest updateConsentRequest) throws ConsentStoreClientException;

    DomesticVRPConsent update2Consent(UpdateDomesticVRPConsentRequest updateConsentRequest) throws ConsentStoreClientException;
}
