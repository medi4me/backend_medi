package com.mediforme.mediforme.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediforme.common.response.ApiResponse;
import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JwtAuthenticationFilter
 * - HTTP 요청 시 JWT 토큰을 검증하고 인증 정보를 SecurityContext에 저장
 * - Redis 기반 블랙리스트 검증 포함
 * - 인증이 불필요한 URL은 자동으로 필터 제외
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    private static final PathMatcher pathMatcher = new AntPathMatcher();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 필터 예외 url
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            // Swagger & API Docs
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",

            // 인증/회원 관련 (JWT 불필요)
            "/auth/**",             // 로그인, 회원가입, 토큰 재발급
            "/find/**",             // 아이디/비밀번호 찾기

            // 정적 리소스
            "/favicon.ico",
            "/error"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return EXCLUDE_URLS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException{
        String requestURI = request.getRequestURI();

        try{
            // 토큰 추출
            String token = jwtTokenProvider.resolveToken(request);
            log.info("[FILTER] tokenHead={}, uri={}",
                token == null ? "null" : token.substring(0, Math.min(15, token.length())),
                request.getRequestURI());

            // 토큰이 없으면 인증 시도 없이 통과 (인증이 필요한지는 Security가 판단)
            if (token == null || token.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            request.setAttribute(SecurityTokenAttributes.ACCESS_TOKEN, token);

            // 블랙리스트 검증 (redis)
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("[FILTER] blacklisted hit: tokenHead={}", token.substring(0, Math.min(15, token.length())));
                throw new CustomApiException(ErrorCode.INVALID_JWT_TOKEN);
            }

            // JWT 유효성 검증 (실패 시 예외 발생)
            jwtTokenProvider.validateTokenOrThrow(token);

            // 인증 객체 생성 및 SecurityContext 저장
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (CustomApiException e) {
            log.warn("JWT 필터 인증 실패: {}", e.getErrorCode());
            writeError(response, e.getErrorCode());

        } catch (Exception e) {
            log.error("JWT 필터 처리 중 알 수 없는 예외", e);
            writeError(response, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // ApiResponse 표준 형식으로 통일
        ApiResponse<Object> body = ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

