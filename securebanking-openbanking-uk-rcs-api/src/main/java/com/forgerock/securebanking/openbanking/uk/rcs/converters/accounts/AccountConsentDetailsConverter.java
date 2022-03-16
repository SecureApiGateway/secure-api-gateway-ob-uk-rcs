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
package com.forgerock.securebanking.openbanking.uk.rcs.converters.accounts;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.details.AccountsConsentDetails;
import com.forgerock.securebanking.openbanking.uk.rcs.converters.general.Converter;
import com.forgerock.securebanking.platform.client.models.accounts.AccountConsentDetails;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

/**
 * Converter class to map {@link AccountConsentDetails} to {@link AccountsConsentDetails}
 */
@Slf4j
@NoArgsConstructor
public class AccountConsentDetailsConverter implements Converter {

    private static volatile AccountConsentDetailsConverter instance;
    private static volatile ModelMapper modelMapperInstance;

    /*
     * Double checked locking principle to ensure that only one instance 'AccountConsentDetailsConverter' is created
     */
    public static AccountConsentDetailsConverter getInstance() {
        if (instance == null) {
            synchronized (AccountConsentDetailsConverter.class) {
                if (instance == null) {
                    instance = new AccountConsentDetailsConverter();
                }
            }
        }
        return instance;
    }

    @Override
    public String getTypeMapName() {
        return AccountConsentDetails.class.getSimpleName() +
                "To" +
                AccountsConsentDetails.class.getSimpleName();
    }

    /*
     * Double checked locking principle to ensure that only one instance 'ModelMapper' is created
     */
    @Override
    public ModelMapper getModelMapper() {
        if (modelMapperInstance == null) {
            synchronized (AccountConsentDetailsConverter.class) {
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
        modelMapper.createTypeMap(AccountConsentDetails.class, AccountsConsentDetails.class, getTypeMapName())
                .addMapping(mapper -> mapper.getData().getPermissions(), AccountsConsentDetails::setPermissions)
                .addMapping(
                        mapper -> mapper.getData().getTransactionFromDateTime()
                        , AccountsConsentDetails::setFromTransaction)
                .addMapping(
                        mapper -> mapper.getData().getTransactionToDateTime()
                        , AccountsConsentDetails::setToTransaction)
                .addMapping(AccountConsentDetails::getAccountIds, AccountsConsentDetails::setAccounts)
                .addMapping(AccountConsentDetails::getOauth2ClientName, AccountsConsentDetails::setAispName)
                .addMapping(
                        mapper -> mapper.getData().getExpirationDateTime()
                        , AccountsConsentDetails::setExpiredDate)
                .addMappings(mapper -> mapper.skip(AccountsConsentDetails::setUserId))
                .addMappings(mapper -> mapper.skip(AccountsConsentDetails::setUsername))
                .addMappings(mapper -> mapper.skip(AccountsConsentDetails::setAccounts))
                .addMappings(mapper -> mapper.skip(AccountsConsentDetails::setClientId));
    }

    public final AccountsConsentDetails toAccountConsentDetails(AccountConsentDetails accountConsentDetails) {
        return getInstance().getModelMapper().map(accountConsentDetails, AccountsConsentDetails.class, getInstance().getTypeMapName());
    }
}
