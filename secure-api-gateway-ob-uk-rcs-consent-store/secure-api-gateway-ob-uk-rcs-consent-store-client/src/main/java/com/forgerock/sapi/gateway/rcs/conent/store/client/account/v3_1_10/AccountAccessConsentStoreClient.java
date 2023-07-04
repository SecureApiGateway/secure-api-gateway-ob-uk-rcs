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
package com.forgerock.sapi.gateway.rcs.conent.store.client.account.v3_1_10;

import com.forgerock.sapi.gateway.rcs.conent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.RejectConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AuthoriseAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;

/**
 * Client for interacting with com.forgerock.sapi.gateway.rcs.consent.store.api.account.v3_1_10.AccountAccessConsentApi
 */
public interface AccountAccessConsentStoreClient {

    AccountAccessConsent createConsent(CreateAccountAccessConsentRequest createConsentRequest) throws ConsentStoreClientException;

    AccountAccessConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException;

    AccountAccessConsent authoriseConsent(AuthoriseAccountAccessConsentRequest authoriseAccountAccessConsentRequest) throws ConsentStoreClientException;

    AccountAccessConsent rejectConsent(RejectConsentRequest rejectAccountAccessConsentRequest) throws ConsentStoreClientException;

    void deleteConsent(String consentId, String apiClientId) throws ConsentStoreClientException;

}
