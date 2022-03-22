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
package com.forgerock.securebanking.openbanking.uk.rcs.web;

import com.forgerock.securebanking.openbanking.uk.rcs.configuration.FilterConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link CorsFilter}.
 */
@ExtendWith(MockitoExtension.class)
public class CorsFilterTest {

    private static final String ALLOWED_HEADERS = "accept-api-version, x-requested-with, " +
            "authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN, Id-Token";
    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS, PATCH";
    private static final String MAX_AGE = "3600";
    private static final String EXPECTED_ORIGIN_ENDS_WITH = "localhost";
    private static final String ORIGIN = "https://" + EXPECTED_ORIGIN_ENDS_WITH;
    @Mock
    private FilterConfigurationProperties filterConfigurationProperties;
    @InjectMocks
    private CorsFilter corsFilter;

    
    public void shouldDoFilterGivenCorsRequest() throws IOException, ServletException {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader(CorsFilter.ORIGIN_HEADER)).willReturn(ORIGIN);
        given(request.getMethod()).willReturn("OPTIONS");
        given(filterConfigurationProperties.getExpectedOriginEndsWith()).willReturn(EXPECTED_ORIGIN_ENDS_WITH);
        given(filterConfigurationProperties.getAllowedHeaders()).willReturn(ALLOWED_HEADERS);
        given(filterConfigurationProperties.getAllowedMethods()).willReturn(ALLOWED_METHODS);
        given(filterConfigurationProperties.getMaxAge()).willReturn(MAX_AGE);
        given(filterConfigurationProperties.isAllowedCredentials()).willReturn(true);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // When
        corsFilter.doFilter(request, response, chain);

        // Then
        verify(response).addHeader("Access-Control-Allow-Origin", ORIGIN);
        verify(response).addHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
        verify(response).addHeader("Access-Control-Max-Age", MAX_AGE);
        verify(response).addHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
        verify(response).addHeader("Access-Control-Allow-Credentials", "true");
        verify(response).setStatus(SC_ACCEPTED);
        verifyNoInteractions(chain);
    }

    
    public void shouldDoFilterGivenNonCorsRequest() throws IOException, ServletException {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader(CorsFilter.ORIGIN_HEADER)).willReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // When
        corsFilter.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
        verifyNoInteractions(response);
    }
}
