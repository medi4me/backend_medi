package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.config.security.jwt.JwtTokenProvider;
import com.mediforme.mediforme.config.security.jwt.SecurityTokenAttributes;
import com.mediforme.mediforme.service.AuthService;
import com.mediforme.mediforme.service.ResignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "회원 탈퇴", description = "회원을 탈퇴시킵니다.(탈퇴 상태 처리)")
public class ResignController {
    private final ResignService resignService;

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리합니다.")
    @DeleteMapping("/resign")
    public ApiResponse<String> resign(HttpServletRequest request) {

        String accessToken = (String) request.getAttribute(SecurityTokenAttributes.ACCESS_TOKEN);

        // 인증 안된 요청이거나 필터 체인/설정 문제인 경우
        if (accessToken == null || accessToken.isBlank()) {
            throw new CustomApiException(ErrorCode.COMMON_UNAUTHORIZED);
        }

        // 회원 탈퇴 처리
        resignService.resignUser(accessToken);
        return ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다.");
    }
}