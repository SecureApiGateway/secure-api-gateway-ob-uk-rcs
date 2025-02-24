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
package com.forgerock.sapi.gateway.rcs.consent.store.client.payment.international;

import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.AuthorisePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.CreateInternationalPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.InternationalPaymentConsent;

/**
 * Client for interacting with com.forgerock.sapi.gateway.rcs.consent.store.api.v3_1_10.InternationalPaymentConsentApi
 */
public interface InternationalPaymentConsentStoreClient {

    InternationalPaymentConsent createConsent(CreateInternationalPaymentConsentRequest createConsentRequest) throws ConsentStoreClientException;

    InternationalPaymentConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException;

    InternationalPaymentConsent authoriseConsent(AuthorisePaymentConsentRequest authorisePaymentConsentRequest) throws ConsentStoreClientException;

    InternationalPaymentConsent rejectConsent(RejectConsentRequest rejectInternationalPaymentConsentRequest) throws ConsentStoreClientException;

    InternationalPaymentConsent consumeConsent(ConsumePaymentConsentRequest consumePaymentConsentRequest) throws ConsentStoreClientException;

}
