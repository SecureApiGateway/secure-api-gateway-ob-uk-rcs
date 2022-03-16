package com.forgerock.securebanking.platform.client.models.domestic.payments;


import com.forgerock.securebanking.platform.client.IntentType;
import com.forgerock.securebanking.platform.client.models.general.Consent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DomesticPaymentConsentDetails extends Consent {

    private DomesticPaymentConsentDataDetails data;
    private List<String> domesticPaymentIds;

    @Override
    public IntentType getIntentType() {
        return IntentType.PAYMENT_DOMESTIC_CONSENT;
    }
}
