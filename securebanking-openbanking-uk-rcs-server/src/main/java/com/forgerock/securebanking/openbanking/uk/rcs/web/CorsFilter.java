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
package com.forgerock.securebanking.openbanking.uk.rcs.web;

import com.forgerock.securebanking.openbanking.uk.rcs.configuration.RcsConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.http.HttpMethod.OPTIONS;

/**
 * Filter to add required CORS related headers to each response.
 */
@Component
@Slf4j
public class CorsFilter implements Filter {

    private static final String ALLOWED_HEADERS = "accept-api-version, x-requested-with, authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN, Id-Token";
    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS";
    private static final Boolean ALLOWED_CREDENTIALS = true;
    private static final String MAX_AGE = "3600";

    private final RcsConfigurationProperties configurationProperties;

    public CorsFilter(RcsConfigurationProperties configurationProperties) {
        this.configurationProperties = configurationProperties;
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (isCorsRequest(request)) {
            log.debug("CORS HTTP Request method: {}", request.getMethod());

            HttpServletResponse response = (HttpServletResponse) servletResponse;
            String originHeader = request.getHeader("Origin");
            URI originUri = URI.create(originHeader);
            String hostRoot = configurationProperties.getHostRoot();
            if (!originUri.getHost().endsWith(hostRoot)) {
                log.warn("Origin header host [{}] does not match the expected host root [{}]", originUri.getHost(), hostRoot);
                response.setStatus(SC_UNAUTHORIZED);
                return;
            }

            response.addHeader("Access-Control-Allow-Origin", originHeader);
            response.addHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
            response.addHeader("Access-Control-Max-Age", MAX_AGE);
            response.addHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
            response.addHeader("Access-Control-Allow-Credentials", ALLOWED_CREDENTIALS.toString());

            // For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS handshake
            if (request.getMethod().equals(OPTIONS.name())) {
                response.setStatus(SC_ACCEPTED);
                return;
            }
        }

        // pass the request along the filter chain
        chain.doFilter(request, servletResponse);
    }

    private static boolean isCorsRequest(HttpServletRequest request) {
        return !StringUtils.isEmpty(request.getHeader("Origin"));
    }
}
