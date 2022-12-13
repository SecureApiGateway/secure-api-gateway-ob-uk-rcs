/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rcs.mapper.decision;

import com.forgerock.securebanking.openbanking.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.securebanking.platform.client.models.ConsentClientDecisionRequest;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Class to map {@link ConsentDecisionDeserialized} to {@link ConsentClientDecisionRequest} to update the consent
 */
@Component
@Slf4j
public class ConsentDecisionMapper {

    /**
     * Map a {@link ConsentDecisionDeserialized} into an instance of type {@link ConsentClientDecisionRequest}
     * @param consentDecisionDeserialized {@link ConsentDecisionDeserialized} to be map to {@link ConsentClientDecisionRequest}
     * @return an instance of type {@link ConsentClientDecisionRequest}
     */
    public ConsentClientDecisionRequest map(ConsentDecisionDeserialized consentDecisionDeserialized) {
        requireNonNull(
                consentDecisionDeserialized,
                "map(ConsentDecisionDeserialized) source parameter 'consentDecisionDeserialized' cannot be null"
        );
        ModelMapper modelMapper = mapper();
        PropertyMap<ConsentDecisionDeserialized, ConsentClientDecisionRequest> decisionMap = new PropertyMap<>() {
            protected void configure() {
                map().getData().setStatus(source.getDecision());
            }
        };
        // source, destination, map name
        modelMapper.createTypeMap(ConsentDecisionDeserialized.class, ConsentClientDecisionRequest.class, getTypeMapName())
                .addMapping(source -> source.getDebtorAccount().getFirstAccount(), ConsentClientDecisionRequest::setDataDebtorAccount)
                .addMapping(source -> source.getConsentJwt(), ConsentClientDecisionRequest::setConsentJwt)
                .addMappings(decisionMap);
        return modelMapper.map(consentDecisionDeserialized, ConsentClientDecisionRequest.class, getTypeMapName());
    }

    private String getTypeMapName(){
        return ConsentDecisionDeserialized.class.getSimpleName() +
                "To" +
                ConsentClientDecisionRequest.class.getSimpleName();
    }
    private ModelMapper mapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
