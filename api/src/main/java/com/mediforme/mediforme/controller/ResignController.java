package com.mediforme.mediforme.controller;

import com.mediforme.common.response.ApiResponse;
import com.mediforme.mediforme.config.security.CurrentUserUtils;
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
@RequestMapping("/users/me")
@Tag(name = "회원 탈퇴(Me)", description = "현재 로그인한 사용자를 탈퇴 처리합니다.")
public class ResignController {
    private final ResignService resignService;

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리합니다.")
    @DeleteMapping("/resign")
    public ApiResponse<String> resign(HttpServletRequest request) {

        String accessToken = CurrentUserUtils.currentAccessTokenOrThrow(request);

        // 회원 탈퇴 처리
        resignService.resignUser(accessToken);
        return ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다.");
    }
}