package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.config.jwt.JwtAuthenticationFilter;
import com.mediforme.mediforme.config.jwt.JwtTokenProvider;
import com.mediforme.mediforme.service.ResignService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resign")
public class ResignController {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ResignService resignService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Operation(summary = "회원 탈퇴 API")
    @PostMapping
    public ApiResponse<String> resign(HttpServletRequest request) {
        String token = jwtAuthenticationFilter.resolveToken(request); // 토큰 추출 (헤더에서)
        String userId = jwtTokenProvider.getMemberIDFromToken(token); // 토큰에서 사용자 ID 추출

        try {
            resignService.resignUser(userId, token);
            return ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다.");
        } catch (CustomApiException e) {
            return ApiResponse.onFailure("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", null);
        }
    }
}