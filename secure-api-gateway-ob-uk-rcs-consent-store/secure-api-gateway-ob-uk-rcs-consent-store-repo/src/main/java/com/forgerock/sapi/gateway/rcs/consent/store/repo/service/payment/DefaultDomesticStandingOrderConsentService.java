package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment;

import org.springframework.stereotype.Service;

import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.DomesticStandingOrderConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.mongo.DomesticStandingOrderConsentRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

@Service
public class DefaultDomesticStandingOrderConsentService extends BasePaymentConsentService<DomesticStandingOrderConsentEntity, PaymentAuthoriseConsentArgs> implements DomesticStandingOrderConsentService {

    public DefaultDomesticStandingOrderConsentService(DomesticStandingOrderConsentRepository repo) {
        super(repo, IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT::generateIntentId);
    }
}
