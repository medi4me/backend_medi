package com.mediforme.mediforme.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 인가 실패(403 Forbidden) 처리
 * - 인증은 되었지만 접근 권한이 없을 때 호출
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // 로그 출력 (요청 URI + 예외 메시지)
        log.warn("[인가 실패] 요청 URI: {}, 예외 메시지: {}", request.getRequestURI(), accessDeniedException.getMessage());

        // 응답 상태 코드 및 헤더 설정
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        // JSON 응답 바디 구성
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", HttpServletResponse.SC_FORBIDDEN);
        responseBody.put("error", "FORBIDDEN");
        responseBody.put("message", ErrorCode.COMMON_FORBIDDEN.getMessage());
        responseBody.put("path", request.getRequestURI());

        // JSON 변환 및 출력
        String jsonResponse = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(jsonResponse);
    }
}

