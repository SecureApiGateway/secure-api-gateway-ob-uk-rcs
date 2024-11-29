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
package com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticstandingorder;

import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.CreateDomesticStandingOrderConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsent;

/**
 * Client for interacting with com.forgerock.sapi.gateway.rcs.consent.store.api.payment.domesticstandingorder.DomesticStandingOrderConsentApi
 */
public interface DomesticStandingOrderConsentStoreClient {

    DomesticStandingOrderConsent createConsent(CreateDomesticStandingOrderConsentRequest createConsentRequest) throws ConsentStoreClientException;

    DomesticStandingOrderConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException;

    DomesticStandingOrderConsent authoriseConsent(AuthorisePaymentConsentRequest authorisePaymentConsentRequest) throws ConsentStoreClientException;

    DomesticStandingOrderConsent rejectConsent(RejectConsentRequest rejectDomesticStandingOrderConsentRequest) throws ConsentStoreClientException;

    DomesticStandingOrderConsent consumeConsent(ConsumePaymentConsentRequest consumePaymentConsentRequest) throws ConsentStoreClientException;

}
