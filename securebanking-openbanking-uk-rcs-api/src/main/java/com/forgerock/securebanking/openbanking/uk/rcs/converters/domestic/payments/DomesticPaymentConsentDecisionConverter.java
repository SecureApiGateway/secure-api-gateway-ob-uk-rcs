/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.converters.domestic.payments;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.ConsentDecisionRequest;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.general.Converter;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentDecision;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentDecision;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;

/**
 * Converter class to map {@link ConsentDecisionRequest} to {@link AccountConsentDecision}
 */
@Slf4j
@NoArgsConstructor
public class DomesticPaymentConsentDecisionConverter implements Converter {

    private static volatile DomesticPaymentConsentDecisionConverter instance;
    private static volatile ModelMapper modelMapperInstance;

    /*
     * Double checked locking principle to ensure that only one instance 'AccountConsentDecisionConverter' is created
     */
    public static DomesticPaymentConsentDecisionConverter getInstance() {
        if (instance == null) {
            synchronized (DomesticPaymentConsentDecisionConverter.class) {
                if (instance == null) {
                    instance = new DomesticPaymentConsentDecisionConverter();
                }
            }
        }
        return instance;
    }

    @Override
    public String getTypeMapName() {
        return ConsentDecisionRequest.class.getSimpleName() +
                "To" +
                AccountConsentDecision.class.getSimpleName();
    }

    /*
     * Double checked locking principle to ensure that only one instance 'ModelMapper' is created
     */
    @Override
    public ModelMapper getModelMapper() {
        if (modelMapperInstance == null) {
            synchronized (DomesticPaymentConsentDecisionConverter.class) {
                if (modelMapperInstance == null) {
                    modelMapperInstance = new ModelMapper();
                    configuration(modelMapperInstance);
                    mapping(modelMapperInstance);
                }
            }
        }
        return modelMapperInstance;
    }

    @Override
    public void configuration(ModelMapper modelMapper) {
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Override
    public void mapping(ModelMapper modelMapper) {
        PropertyMap<ConsentDecisionRequest, DomesticPaymentConsentDecision> decisionMap = new PropertyMap<>() {
            protected void configure() {
                map().getData().setStatus(source.getDecision());
            }
        };
        modelMapper.createTypeMap(ConsentDecisionRequest.class, DomesticPaymentConsentDecision.class, getTypeMapName())
                .addMapping(source -> source.getConsentJwt(), DomesticPaymentConsentDecision::setConsentJwt)
                .addMappings(decisionMap);
    }

    public final DomesticPaymentConsentDecision toDomesticPaymentConsentDecision(
            ConsentDecisionRequest consentDecision) {
        return getInstance().getModelMapper().map(
                consentDecision,
                DomesticPaymentConsentDecision.class,
                getInstance().getTypeMapName()
        );
    }
}
