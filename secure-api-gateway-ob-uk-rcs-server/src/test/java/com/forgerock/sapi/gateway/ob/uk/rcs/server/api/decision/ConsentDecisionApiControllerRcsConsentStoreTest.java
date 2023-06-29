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
package com.forgerock.sapi.gateway.ob.uk.rcs.server.api.decision;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVRPConsentConverters;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.RedirectionAction;
import com.forgerock.sapi.gateway.ob.uk.rcs.api.dto.consent.decision.ConsentDecisionDeserialized;
import com.forgerock.sapi.gateway.ob.uk.rcs.cloud.client.Constants;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.RCSServerApplicationTestSupport;
import com.forgerock.sapi.gateway.ob.uk.rcs.server.testsupport.JwtTestHelper;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.ConsentStoreEnabledIntentTypes;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.account.AccountAccessConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.BasePaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.domestic.DomesticPaymentConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.entity.payment.vrp.DomesticVRPConsentEntity;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.account.AccountAccessConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.PaymentConsentStateModel;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.domestic.DomesticPaymentConsentService;
import com.forgerock.sapi.gateway.rcs.consent.store.repo.service.payment.vrp.DomesticVRPConsentService;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;

import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadConsent1;
import uk.org.openbanking.datamodel.account.OBReadData1;
import uk.org.openbanking.datamodel.account.OBRisk2;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpConsentRequestTestDataFactory;

/**
 * Spring Boot Test for {@link ConsentDecisionApiController} using the RCS Consent Store.
 */
@EnableConfigurationProperties
@ActiveProfiles("test")
@SpringBootTest(classes = RCSServerApplicationTestSupport.class, webEnvironment = RANDOM_PORT)
public class ConsentDecisionApiControllerRcsConsentStoreTest {

    private static final String TEST_API_CLIENT_ID = "test-api-client-1";

    private static final String TEST_RESOURCE_OWNER_ID = "test-resource-owner-1";

    private static final List<String> TEST_ACCOUNT_ACCESS_ACCOUNT_IDS = List.of("acc-1", "acc-2");

    private static final String TEST_PAYMENT_DEBTOR_ACC_ID = "debtor-acc-1";

    @LocalServerPort
    private int port;

    private String consentDecisionUrl;

    @PostConstruct
    void constructConsentDecisionUrl() {
        consentDecisionUrl = "http://localhost:" + port + "/rcs/api/consent/decision";
    }

    @Autowired
    private ConsentStoreEnabledIntentTypes consentStoreEnabledIntentTypes;

    @Autowired
    private AccountAccessConsentService accountAccessConsentService;

    @Autowired
    private DomesticPaymentConsentService domesticPaymentConsentService;

    @Autowired
    private DomesticVRPConsentService domesticVRPConsentService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${rcs.consent.response.jwt.signingKeyId}")
    private String expectedSigningKeyId;

    @Value("${rcs.consent.response.jwt.signingAlgorithm}")
    private String expectedSigningAlgorithm;

    @Value("${rcs.consent.response.jwt.issuer}")
    private String expectedConsentResponseJwtIssuer;

    private final JWSVerifier jwsVerifier;

    public ConsentDecisionApiControllerRcsConsentStoreTest(@Value("${rcs.consent.response.jwt.privateKeyPath}") Path privateKeyPath) throws Exception {
        final JWK jwk = JWK.parseFromPEMEncodedObjects(Files.readString(privateKeyPath));
        jwsVerifier = new RSASSAVerifier((RSAKey) jwk);
    }

    @Test
    public void testAuthoriseAccountAccessConsent() {
        Assumptions.assumeTrue(consentStoreEnabledIntentTypes.isIntentTypeSupported(IntentType.ACCOUNT_ACCESS_CONSENT));

        // Create an AccountAccessConsent in the store
        final AccountAccessConsentEntity consent = new AccountAccessConsentEntity();
        consent.setApiClientId(TEST_API_CLIENT_ID);
        consent.setRequestVersion(OBVersion.v3_1_10);
        consent.setStatus(AccountAccessConsentStateModel.AWAITING_AUTHORISATION);
        consent.setRequestObj(new OBReadConsent1().data(new OBReadData1().permissions(
                List.of(OBExternalPermissions1Code.READACCOUNTSBASIC))).risk(new OBRisk2()));
        final AccountAccessConsentEntity persistedConsent = accountAccessConsentService.createConsent(consent);

        final String consentRequestJwt = JwtTestHelper.consentRequestJwt(TEST_API_CLIENT_ID, persistedConsent.getId(), TEST_RESOURCE_OWNER_ID);

        final ConsentDecisionDeserialized consentDecisionDeserialized = ConsentDecisionDeserialized.builder()
                .accountIds(TEST_ACCOUNT_ACCESS_ACCOUNT_IDS)
                .consentJwt(consentRequestJwt)
                .decision(Constants.ConsentDecisionStatus.AUTHORISED)
                .build();
        final HttpEntity<ConsentDecisionDeserialized> request = new HttpEntity<>(consentDecisionDeserialized, headers());

        final ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDecisionUrl, request, RedirectionAction.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        final String consentResponseJwt = response.getBody().getConsentJwt();
        assertThat(consentResponseJwt).isNotEmpty();
        verifyConsentResponseJwt(consentResponseJwt);

        // Verify consent in store is now authorised
        final AccountAccessConsentEntity authorisedConsent = accountAccessConsentService.getConsent(persistedConsent.getId(), persistedConsent.getApiClientId());
        assertEquals(AccountAccessConsentStateModel.AUTHORISED, authorisedConsent.getStatus());
        assertEquals(TEST_RESOURCE_OWNER_ID, authorisedConsent.getResourceOwnerId());
        assertEquals(TEST_ACCOUNT_ACCESS_ACCOUNT_IDS, authorisedConsent.getAuthorisedAccountIds());
    }

    private static Stream<Arguments> paymentConsentArguments() {
        return Stream.of(
                arguments(IntentType.PAYMENT_DOMESTIC_CONSENT)
        );
    }

    private BasePaymentConsentEntity createPaymentConsent(IntentType intentType) {
        switch (intentType) {
        case PAYMENT_DOMESTIC_CONSENT:
            return createDomesticPaymentConsentEntity();
        case DOMESTIC_VRP_PAYMENT_CONSENT:
            return createDomesticVRPConsentEntity();
        }
        throw new UnsupportedOperationException();
    }

    private PaymentConsentService getConsentService(IntentType intentType) {
        switch (intentType) {
        case PAYMENT_DOMESTIC_CONSENT:
            return domesticPaymentConsentService;
        case DOMESTIC_VRP_PAYMENT_CONSENT:
            return domesticVRPConsentService;
        }
        throw new UnsupportedOperationException();
    }

    @ParameterizedTest
    @MethodSource("paymentConsentArguments")
    public void testAuthorisePaymentConsent(IntentType intentType) {
        Assumptions.assumeTrue(consentStoreEnabledIntentTypes.isIntentTypeSupported(intentType));

        final BasePaymentConsentEntity consent = createPaymentConsent(intentType);
        final String consentRequestJwt = JwtTestHelper.consentRequestJwt(TEST_API_CLIENT_ID, consent.getId(), TEST_RESOURCE_OWNER_ID);

        final FRFinancialAccount debtorAccount = new FRFinancialAccount();
        debtorAccount.setAccountId(TEST_PAYMENT_DEBTOR_ACC_ID);
        final ConsentDecisionDeserialized consentDecisionDeserialized = ConsentDecisionDeserialized.builder()
                .debtorAccount(debtorAccount)
                .consentJwt(consentRequestJwt)
                .decision(Constants.ConsentDecisionStatus.AUTHORISED)
                .build();
        final HttpEntity<ConsentDecisionDeserialized> request = new HttpEntity<>(consentDecisionDeserialized, headers());

        final ResponseEntity<RedirectionAction> response = restTemplate.postForEntity(consentDecisionUrl, request, RedirectionAction.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        final String consentResponseJwt = response.getBody().getConsentJwt();
        assertThat(consentResponseJwt).isNotEmpty();
        verifyConsentResponseJwt(consentResponseJwt);

        // Verify consent in store is now authorised
        final PaymentConsentService consentService = getConsentService(intentType);
        final BasePaymentConsentEntity authorisedConsent = (BasePaymentConsentEntity) consentService.getConsent(consent.getId(), consent.getApiClientId());
        assertEquals(PaymentConsentStateModel.AUTHORISED, authorisedConsent.getStatus());
        assertEquals(TEST_RESOURCE_OWNER_ID, authorisedConsent.getResourceOwnerId());
        assertEquals(TEST_PAYMENT_DEBTOR_ACC_ID, authorisedConsent.getAuthorisedDebtorAccountId());
    }

    DomesticPaymentConsentEntity createDomesticPaymentConsentEntity() {
        final DomesticPaymentConsentEntity consent = new DomesticPaymentConsentEntity();
        consent.setApiClientId(TEST_API_CLIENT_ID);
        consent.setRequestVersion(OBVersion.v3_1_10);
        consent.setStatus(PaymentConsentStateModel.AWAITING_AUTHORISATION);
        consent.setRequestObj(FRWriteDomesticConsentConverter.toFRWriteDomesticConsent(OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4()));
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        return domesticPaymentConsentService.createConsent(consent);
    }

    DomesticVRPConsentEntity createDomesticVRPConsentEntity() {
        final DomesticVRPConsentEntity consent = new DomesticVRPConsentEntity();
        consent.setApiClientId(TEST_API_CLIENT_ID);
        consent.setRequestVersion(OBVersion.v3_1_10);
        consent.setStatus(PaymentConsentStateModel.AWAITING_AUTHORISATION);
        consent.setRequestObj(FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest()));
        consent.setIdempotencyKey(UUID.randomUUID().toString());
        consent.setIdempotencyKeyExpiration(DateTime.now().plusDays(1));
        return domesticVRPConsentService.createConsent(consent);
    }


    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

    private void verifyConsentResponseJwt(String consentResponseJwt) {
        assertNotNull(consentResponseJwt);
        try {
            final JWSObject parsedConsent = JWSObject.parse(consentResponseJwt);
            assertEquals(expectedSigningAlgorithm, parsedConsent.getHeader().getAlgorithm().getName());
            assertEquals(expectedSigningKeyId, parsedConsent.getHeader().getKeyID());
            final JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(parsedConsent.getPayload().toJSONObject());
            assertEquals(expectedConsentResponseJwtIssuer, jwtClaimsSet.getIssuer());
            assertTrue(parsedConsent.verify(jwsVerifier), "consentResponseJwt sig failed validation");
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
