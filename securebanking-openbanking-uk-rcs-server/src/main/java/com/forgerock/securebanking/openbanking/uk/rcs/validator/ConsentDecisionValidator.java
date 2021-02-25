/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.validator;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.tpp.Tpp;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.rcs.client.idm.TppService;
import com.forgerock.securebanking.openbanking.uk.rcs.client.am.UserProfileService;
import com.forgerock.securebanking.openbanking.uk.rcs.service.decision.ConsentDecisionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType.*;

@Service
@Slf4j
public class ConsentDecisionValidator {

    private final UserProfileService userProfileService;
    private final TppService tppService;

    public ConsentDecisionValidator(UserProfileService userProfileService, TppService tppService) {
        this.userProfileService = userProfileService;
        this.tppService = tppService;
    }

    public void validateConsent(String intentId,
                                String clientId,
                                String ssoToken,
                                ConsentDecisionService consentDecisionService) throws OBErrorException {

        verifyConsentBelongsToTpp(intentId, clientId, consentDecisionService);
        verifyConsentDecisionSentBySameUser(ssoToken, intentId, consentDecisionService);
    }

    private void verifyConsentBelongsToTpp(String intentId,
                                           String clientId,
                                           ConsentDecisionService decisionService) throws OBErrorException {
        //Verify consent is own by the right TPP
        String tppIdBehindConsent = decisionService.getTppIdBehindConsent(intentId);
        Optional<Tpp> isTpp = tppService.findById(tppIdBehindConsent);
        if (!isTpp.isPresent()) {
            log.error("The TPP '{}' that created this intent id '{}' doesn't exist anymore.", tppIdBehindConsent, intentId);
            throw new OBErrorException(RCS_CONSENT_REQUEST_NOT_FOUND_TPP, tppIdBehindConsent, intentId, clientId);
        }

        if (!clientId.equals(isTpp.get().getClientId())) {
            log.error("The TPP '{}' created the account request '{}' but it's TPP '{}' that is trying to get" +
                    " consent for it.", tppIdBehindConsent, intentId, clientId);
            throw new OBErrorException(RCS_CONSENT_REQUEST_INVALID_CONSENT, tppIdBehindConsent, intentId, clientId);
        }
    }

    private void verifyConsentDecisionSentBySameUser(String ssoToken,
                                                     String intentId,
                                                     ConsentDecisionService decisionService) throws OBErrorException {
        String username = userProfileService.getUsername(ssoToken);
        String userIdBehindConsent = decisionService.getUserIdBehindConsent(intentId);

        if (!username.equals(userIdBehindConsent)) {
            log.error("The consent was associated with user '{}' but now, its user '{}' that " +
                    "send the consent decision.", userIdBehindConsent, username);
            throw new OBErrorException(RCS_CONSENT_DECISION_INVALID_USER, userIdBehindConsent, username);
        }
    }
}
