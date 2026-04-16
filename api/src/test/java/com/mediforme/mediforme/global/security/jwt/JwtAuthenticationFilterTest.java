package com.mediforme.mediforme.global.security.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.global.security.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("토큰 없음 → 인증 설정 없이 다음 필터로 통과, 블랙리스트 조회 안함")
    void doFilter_noToken_passesWithoutAuth() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        given(jwtTokenProvider.resolveToken(request)).willReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenBlacklistService, never()).isTokenBlacklisted(anyString());
    }

    @Test
    @DisplayName("제외 URL (/auth/login) → shouldNotFilter = true")
    void shouldNotFilter_authEndpoint_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    @DisplayName("제외 URL (/swagger-ui/index.html) → shouldNotFilter = true")
    void shouldNotFilter_swaggerUi_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    @DisplayName("보호 URL (/users/me) → shouldNotFilter = false")
    void shouldNotFilter_protectedEndpoint_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    @DisplayName("정상 토큰 → SecurityContext 에 Authentication 설정 + ACCESS_TOKEN attribute 세팅")
    void doFilter_validToken_setsAuthenticationAndAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        String token = "valid.jwt.token";
        Authentication auth = new UsernamePasswordAuthenticationToken("user01", null, List.of());
        given(jwtTokenProvider.resolveToken(request)).willReturn(token);
        given(tokenBlacklistService.isTokenBlacklisted(token)).willReturn(false);
        given(jwtTokenProvider.validateTokenOrThrow(token)).willReturn(true);
        given(jwtTokenProvider.getAuthentication(token)).willReturn(auth);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(auth);
        assertThat(request.getAttribute(SecurityTokenAttributes.ACCESS_TOKEN)).isEqualTo(token);
    }

    @Test
    @DisplayName("블랙리스트 토큰 → 401 JSON 응답 (INVALID_JWT_TOKEN) + 체인 중단")
    void doFilter_blacklistedToken_writes401Json() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        String token = "blacklisted.token";
        given(jwtTokenProvider.resolveToken(request)).willReturn(token);
        given(tokenBlacklistService.isTokenBlacklisted(token)).willReturn(true);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_JWT_TOKEN.getHttpStatus().value());
        JsonNode body = new ObjectMapper().readTree(response.getContentAsString());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("code").asText()).isEqualTo(ErrorCode.INVALID_JWT_TOKEN.getCode());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("만료 토큰 → 401 JSON (EXPIRED_JWT_TOKEN)")
    void doFilter_expiredToken_writes401Json() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        String token = "expired.token";
        given(jwtTokenProvider.resolveToken(request)).willReturn(token);
        given(tokenBlacklistService.isTokenBlacklisted(token)).willReturn(false);
        given(jwtTokenProvider.validateTokenOrThrow(token))
            .willThrow(new CustomApiException(ErrorCode.EXPIRED_JWT_TOKEN));

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(ErrorCode.EXPIRED_JWT_TOKEN.getHttpStatus().value());
        JsonNode body = new ObjectMapper().readTree(response.getContentAsString());
        assertThat(body.get("code").asText()).isEqualTo(ErrorCode.EXPIRED_JWT_TOKEN.getCode());
    }

    @Test
    @DisplayName("위조 토큰 → 401 JSON (INVALID_JWT_TOKEN)")
    void doFilter_invalidToken_writes401Json() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        String token = "bad.token";
        given(jwtTokenProvider.resolveToken(request)).willReturn(token);
        given(tokenBlacklistService.isTokenBlacklisted(token)).willReturn(false);
        given(jwtTokenProvider.validateTokenOrThrow(token))
            .willThrow(new CustomApiException(ErrorCode.INVALID_JWT_TOKEN));

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_JWT_TOKEN.getHttpStatus().value());
        JsonNode body = new ObjectMapper().readTree(response.getContentAsString());
        assertThat(body.get("code").asText()).isEqualTo(ErrorCode.INVALID_JWT_TOKEN.getCode());
    }

    @Test
    @DisplayName("예상치 못한 예외 → 500 JSON (COMMON500)")
    void doFilter_unexpectedError_writes500Json() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        given(jwtTokenProvider.resolveToken(request)).willThrow(new RuntimeException("boom"));

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus())
            .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value());
        JsonNode body = new ObjectMapper().readTree(response.getContentAsString());
        assertThat(body.get("code").asText()).isEqualTo("COMMON500");
    }
}
