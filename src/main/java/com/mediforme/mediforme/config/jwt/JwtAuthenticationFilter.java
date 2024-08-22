package com.mediforme.mediforme.config.jwt;

import com.mediforme.mediforme.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    private static final List<Pattern> EXCLUDE_URL_PATTERNS = Arrays.asList(
            Pattern.compile("^/v3/.*"),
            Pattern.compile("^/swagger-ui/.*"),
            Pattern.compile("^/register/.*"),
            Pattern.compile("^/auth/.*"),
            Pattern.compile("^/test/.*"),
            Pattern.compile("^/find/.*"),
            Pattern.compile("^/favicon.ico$")
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return EXCLUDE_URL_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 필터를 적용하지 않아야 하는 URL인 경우, 필터를 건너뜁니다.
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        else {
            String token = resolveToken(request);
            if ((token != null && !tokenBlacklistService.isTokenBlacklisted(token))) {
                String jwt = resolveToken(request);

                // token 유효성 검사
                if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                    System.out.println("여기가 문제일 수 도????");
                    Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

                System.out.println("필터 문제인가?");
                System.out.println(request);
                System.out.println(response);
                filterChain.doFilter(request, response);
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("Token is invalid or blacklisted.");
                return;
            }
        }
    }

    // 요청 헤더에서 토큰을 꺼내는 함수
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
