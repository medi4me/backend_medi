package com.mediforme.mediforme.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediforme.common.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * 인증 실패(401 Unauthorized) 처리
 * - 인증 토큰이 없거나, 유효하지 않은 경우 호출
 * - SecurityContextHolder에 인증 정보가 없는 상태
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // 로그 출력 (요청 url + 예외 메시지)
        log.warn("[인증 실패] 요청 URI: {}, 예외 메시지: {}", request.getRequestURI(), authException.getMessage());

        // 응답 상태 코드 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // JSON 응답 바디 구성
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        responseBody.put("error", "UNAUTHORIZED");
        responseBody.put("message", ErrorCode.INVALID_JWT_TOKEN.getMessage());
        responseBody.put("path", request.getRequestURI());

        // JSON 변환 및 출력
        String jsonResponse = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(jsonResponse);
    }
}
