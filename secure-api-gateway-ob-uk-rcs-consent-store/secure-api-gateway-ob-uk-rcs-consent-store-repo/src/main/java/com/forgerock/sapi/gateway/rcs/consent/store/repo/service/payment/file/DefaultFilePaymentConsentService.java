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
package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.file;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.file.FilePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.exception.ConsentStoreException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.payment.PaymentConsentRepository;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.BasePaymentConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentAuthoriseConsentArgs;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

public class DefaultFilePaymentConsentService extends BasePaymentConsentService<FilePaymentConsentEntity, PaymentAuthoriseConsentArgs> implements FilePaymentConsentService {

    public DefaultFilePaymentConsentService(PaymentConsentRepository<FilePaymentConsentEntity> repo) {
        super(repo, IntentType.PAYMENT_FILE_CONSENT::generateIntentId, FilePaymentConsentStateModel.getInstance());
    }

    @Override
    public FilePaymentConsentEntity uploadFile(FileUploadArgs fileUploadArgs) {
        final FilePaymentConsentEntity consent = getConsent(fileUploadArgs.getConsentId(), fileUploadArgs.getApiClientId());

        // fileUpload idempotency check
        if (consent.getStatus().equals(FilePaymentConsentStateModel.AWAITING_AUTHORISATION)) {
           if (consent.getFileUploadIdempotencyKey().equals(fileUploadArgs.getFileUploadIdempotencyKey())
                   && consent.getFileContent().equals(fileUploadArgs.getFileContents())) {
               return consent;
           } else {
               throw new ConsentStoreException(ErrorType.IDEMPOTENCY_ERROR, fileUploadArgs.getConsentId(),
                       "File has already been uploaded for this consent but the fileUploadIdempotencyKey and fileContents do not match with this request");
           }
        } else {
            validateStateTransition(consent, FilePaymentConsentStateModel.AWAITING_AUTHORISATION);
            consent.setStatus(FilePaymentConsentStateModel.AWAITING_AUTHORISATION);
            consent.setFileContent(fileUploadArgs.getFileContents());
            consent.setFileUploadIdempotencyKey(fileUploadArgs.getFileUploadIdempotencyKey());
            return repo.save(consent);
        }
    }
}
