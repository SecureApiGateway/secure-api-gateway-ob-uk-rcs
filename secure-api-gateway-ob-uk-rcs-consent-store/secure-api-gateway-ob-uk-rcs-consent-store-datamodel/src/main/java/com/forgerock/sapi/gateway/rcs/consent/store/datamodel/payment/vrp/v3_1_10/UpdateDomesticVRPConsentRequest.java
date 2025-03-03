package com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10;

import org.springframework.validation.annotation.Validated;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVRPConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.BaseCreatePaymentConsentRequest;

@Validated
public class UpdateDomesticVRPConsentRequest extends BaseCreatePaymentConsentRequest<FRDomesticVRPConsent> {
}
