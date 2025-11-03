package com.mediforme.mediforme.config.security.jwt;

import com.mediforme.lib.redis.repository.BlacklistRedisRepository;
import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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
    private final BlacklistRedisRepository blacklistRedisRepository;
    private static final PathMatcher pathMatcher = new AntPathMatcher();

    // 필터 예외 url
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            // Swagger & API Docs
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",

            // 인증/회원 관련 (JWT 불필요)
            "/v2/users/auth/**",    // 로그인, 회원가입, 토큰 재발급
            "/v2/find/**",          // 아이디/비밀번호 찾기

            // 정적 리소스
            "/favicon.ico",
            "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException{
        String requestURI = request.getRequestURI();

        // 인증 제외 경로면 필터 패스
        if (isExcluded(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        try{
            // 토큰 추출
            String token = jwtTokenProvider.resolveToken(request);
            if (token == null){
                throw new CustomApiException(ErrorCode.EMPTY_JWT_CLAIMS);
            }
            // 블랙리스트 검증
            if (blacklistRedisRepository.findById(token).isPresent()){
                throw new CustomApiException(ErrorCode.INVALID_JWT_TOKEN);
            }
            // 유효한 토큰이면 Authentication 객체 생성
            if (jwtTokenProvider.validateToken(token)){
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 다음 필터 실행
            filterChain.doFilter(request, response);
        }  catch (ExpiredJwtException e) {
            log.warn("JWT 만료: {}", e.getMessage());
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        } catch (CustomApiException e) {
            log.warn("JWT 검증 실패: {}", e.getErrorCode().getMessage());
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("JWT 필터 처리 중 예외 발생", e);
            setErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }


    /** 특정 경로가 인증 제외 대상인지 여부 */
    private boolean isExcluded(String requestURI) {
        return JwtAuthenticationFilter.EXCLUDE_URLS.stream().anyMatch(pattern -> JwtAuthenticationFilter.pathMatcher.match(pattern, requestURI));
    }

    /** 공통 에러 응답 */
    private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}

