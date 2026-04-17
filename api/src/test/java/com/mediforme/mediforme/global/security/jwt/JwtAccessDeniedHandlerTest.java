package com.mediforme.mediforme.global.security.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediforme.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAccessDeniedHandlerTest {

    private final JwtAccessDeniedHandler handler = new JwtAccessDeniedHandler();

    @Test
    @DisplayName("권한 부족 시 403 + ApiResponse 포맷 (code=ACTION403) 응답")
    void handle_writesApiResponseJsonWith403() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("forbidden"));

        assertThat(response.getStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACTION.getHttpStatus().value());
        JsonNode body = new ObjectMapper().readTree(response.getContentAsString());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("code").asText()).isEqualTo(ErrorCode.FORBIDDEN_ACTION.getCode());
        assertThat(body.get("message").asText()).isEqualTo(ErrorCode.FORBIDDEN_ACTION.getMessage());
    }
}
