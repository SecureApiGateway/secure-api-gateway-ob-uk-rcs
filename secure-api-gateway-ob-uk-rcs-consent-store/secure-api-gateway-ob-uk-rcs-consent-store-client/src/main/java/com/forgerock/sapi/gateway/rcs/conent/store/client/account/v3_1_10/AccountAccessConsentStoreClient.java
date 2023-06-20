package com.forgerock.sapi.gateway.rcs.conent.store.client.account.v3_1_10;

import com.forgerock.sapi.gateway.rcs.conent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AuthoriseAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.RejectAccountAccessConsentRequest;

public interface AccountAccessConsentStoreClient {

    AccountAccessConsent createConsent(CreateAccountAccessConsentRequest createConsentRequest) throws ConsentStoreClientException;

    AccountAccessConsent getConsent(String consentId, String apiClientId) throws ConsentStoreClientException;

    AccountAccessConsent authoriseConsent(AuthoriseAccountAccessConsentRequest authoriseAccountAccessConsentRequest) throws ConsentStoreClientException;

    AccountAccessConsent rejectConsent(RejectAccountAccessConsentRequest rejectAccountAccessConsentRequest) throws ConsentStoreClientException;

}
