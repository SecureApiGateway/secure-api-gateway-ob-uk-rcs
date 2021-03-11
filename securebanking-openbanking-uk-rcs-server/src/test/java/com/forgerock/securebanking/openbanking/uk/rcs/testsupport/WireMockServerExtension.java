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
package com.forgerock.securebanking.openbanking.uk.rcs.testsupport;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.util.SocketUtils;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * JUnit 5 extension class to ensure WireMock is started and stopped before and after each set of tests.
 */
public class WireMockServerExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

    public static final int SERVER_PORT = SocketUtils.findAvailableTcpPort();
    public static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(wireMockConfig().port(SERVER_PORT));

    @Override
    public void beforeAll(ExtensionContext context) {
        WIRE_MOCK_SERVER.start();
    }

    @Override
    public void beforeEach(final ExtensionContext extensionContext) {
        WIRE_MOCK_SERVER.resetAll();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        WIRE_MOCK_SERVER.stop();
    }
}
