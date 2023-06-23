package com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticStandingOrderConsent;

/**
 * OBIE Domestic Standing Order Consent: https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/pisp/domestic-standing-order-consents.html
 */
@Document("DomesticStandingOrderConsent")
@Validated
public class DomesticStandingOrderConsentEntity extends BasePaymentConsentEntity<FRWriteDomesticStandingOrderConsent> {
}
