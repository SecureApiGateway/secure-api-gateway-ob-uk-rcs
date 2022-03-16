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

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.DomesticPaymentsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.general.Converter;
import com.forgerock.securebanking.platform.client.models.domestic.payments.DomesticPaymentConsentDetails;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

/**
 * Converter class to map {@link DomesticPaymentConsentDetails} to {@link DomesticPaymentConsentDetails}
 */
@Slf4j
@NoArgsConstructor
public class DomesticPaymentConsentDetailsConverter implements Converter {

    private static volatile DomesticPaymentConsentDetailsConverter instance;
    private static volatile ModelMapper modelMapperInstance;

    /*
     * Double checked locking principle to ensure that only one instance 'DomesticPaymentConsentDetailsConverter' is created
     */
    public static DomesticPaymentConsentDetailsConverter getInstance() {
        if (instance == null) {
            synchronized (DomesticPaymentConsentDetailsConverter.class) {
                if (instance == null) {
                    instance = new DomesticPaymentConsentDetailsConverter();
                }
            }
        }
        return instance;
    }

    @Override
    public String getTypeMapName() {
        return DomesticPaymentConsentDetails.class.getSimpleName() +
                "To" +
                DomesticPaymentConsentDetails.class.getSimpleName();
    }

    /*
     * Double checked locking principle to ensure that only one instance 'ModelMapper' is created
     */
    @Override
    public ModelMapper getModelMapper() {
        if (modelMapperInstance == null) {
            synchronized (DomesticPaymentConsentDetailsConverter.class) {
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
        modelMapper.createTypeMap(DomesticPaymentConsentDetails.class, DomesticPaymentsConsentDetails.class, getTypeMapName())
                .addMapping(mapper -> mapper.getData().getInitiation(), DomesticPaymentsConsentDetails::setInitiation)
                .addMapping(
                        mapper -> mapper.getData().getCutOffDateTime()
                        , DomesticPaymentsConsentDetails::setCutOffDateTime)
                .addMapping(
                        mapper -> mapper.getData().getExpectedExecutionDateTime()
                        , DomesticPaymentsConsentDetails::setExpectedExecutionDateTime)
                .addMapping(mapper -> mapper.getData().getExpectedSettlementDateTime(), DomesticPaymentsConsentDetails::setExpectedSettlementDateTime)
                .addMapping(
                        mapper -> mapper.getData().getCharges()
                        , DomesticPaymentsConsentDetails::setCharges)
                .addMapping(
                        mapper -> mapper.getData().getInitiation()
                        , DomesticPaymentsConsentDetails::setInitiation)
                .addMapping(
                        mapper -> mapper.getData().getAuthorisation()
                        , DomesticPaymentsConsentDetails::setAuthorisation)
                .addMapping(DomesticPaymentConsentDetails::getOauth2ClientName, DomesticPaymentsConsentDetails::setPispName)
                .addMappings(mapper -> mapper.skip(DomesticPaymentsConsentDetails::setUserId))
                .addMappings(mapper -> mapper.skip(DomesticPaymentsConsentDetails::setUsername))
                .addMappings(mapper -> mapper.skip(DomesticPaymentsConsentDetails::setInitiation));
    }

    public final DomesticPaymentsConsentDetails toDomesticPaymentConsentDetails(DomesticPaymentConsentDetails consentDetails) {
        return getInstance().getModelMapper().map(consentDetails, DomesticPaymentsConsentDetails.class, getInstance().getTypeMapName());
    }
}
