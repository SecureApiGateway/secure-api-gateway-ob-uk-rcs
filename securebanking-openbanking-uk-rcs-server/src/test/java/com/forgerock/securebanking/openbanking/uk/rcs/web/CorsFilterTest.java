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

    @Mock
    private RcsConfigurationProperties configurationProperties;

    @InjectMocks
    private CorsFilter corsFilter;

    @Test
    public void shouldDoFilterGivenCorsRequest() throws IOException, ServletException {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("Origin")).willReturn("http://localhost");
        given(request.getMethod()).willReturn("OPTIONS");
        given(configurationProperties.getHostRoot()).willReturn("localhost");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // When
        corsFilter.doFilter(request, response, chain);

        // Then
        verify(response).addHeader("Access-Control-Allow-Origin", "http://localhost");
        verify(response).addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
        verify(response).addHeader("Access-Control-Max-Age", "3600");
        verify(response).addHeader("Access-Control-Allow-Headers", "accept-api-version, x-requested-with, " +
                "authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN, Id-Token");
        verify(response).addHeader("Access-Control-Allow-Credentials", "true");
        verify(response).setStatus(SC_ACCEPTED);
        verifyNoInteractions(chain);
    }

    @Test
    public void shouldDoFilterGivenNonCorsRequest() throws IOException, ServletException {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("Origin")).willReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // When
        corsFilter.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
        verifyNoInteractions(response);
    }
}