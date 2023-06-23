package com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRChargeBearerType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticStandingOrderConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.DomesticStandingOrderConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.BaseConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticStandingOrderConsentTestDataFactory;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class DefaultDomesticStandingOrderConsentServiceTest extends BasePaymentConsentServiceTest<DomesticStandingOrderConsentEntity> {

    @Autowired
    private DefaultDomesticStandingOrderConsentService service;

    @Override
    protected BaseConsentService<DomesticStandingOrderConsentEntity, PaymentAuthoriseConsentArgs> getConsentServiceToTest() {
        return service;
    }

    @Override
    protected DomesticStandingOrderConsentEntity getValidConsentEntity() {
        final String apiClientId = "test-client-987";
        return createValidConsentEntity(apiClientId);
    }

    public static DomesticStandingOrderConsentEntity createValidConsentEntity(String apiClientId) {
        return createValidConsentEntity(OBWriteDomesticStandingOrderConsentTestDataFactory.aValidOBWriteDomesticStandingOrderConsent5(), apiClientId);
    }

    public static DomesticStandingOrderConsentEntity createValidConsentEntity(OBWriteDomesticStandingOrderConsent5 obConsent, String apiClientId) {
        final DomesticStandingOrderConsentEntity domesticStandingOrderConsent = new DomesticStandingOrderConsentEntity();
        domesticStandingOrderConsent.setRequestVersion(OBVersion.v3_1_10);
        domesticStandingOrderConsent.setApiClientId(apiClientId);
        domesticStandingOrderConsent.setRequestObj(FRWriteDomesticStandingOrderConsentConverter.toFRWriteDomesticStandingOrderConsent(obConsent));
        domesticStandingOrderConsent.setStatus(StatusEnum.AWAITINGAUTHORISATION.toString());
        domesticStandingOrderConsent.setIdempotencyKey(UUID.randomUUID().toString());
        domesticStandingOrderConsent.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        domesticStandingOrderConsent.setCharges(List.of(
                FRCharge.builder().type("fee1")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.12", "GBP"))
                        .build(),
                FRCharge.builder().type("fee2")
                        .chargeBearer(FRChargeBearerType.BORNEBYDEBTOR)
                        .amount(new FRAmount("0.12", "GBP"))
                        .build())
        );
        return domesticStandingOrderConsent;
    }
}