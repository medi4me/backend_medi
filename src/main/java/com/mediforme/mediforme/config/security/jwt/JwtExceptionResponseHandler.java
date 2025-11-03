package com.mediforme.mediforme.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 예외 발생 시 일관된 JSON 응답 반환
 */
@Slf4j
@Component
public class JwtExceptionResponseHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setErrorResponse(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode) throws IOException {
        log.warn("JWT 예외 발생 - {}: {}", errorCode.getCode(), errorCode.getMessage());

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", errorCode.getHttpStatus().value());
        responseBody.put("error", errorCode.name());
        responseBody.put("code", errorCode.getCode());
        responseBody.put("message", errorCode.getMessage());
        responseBody.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}
