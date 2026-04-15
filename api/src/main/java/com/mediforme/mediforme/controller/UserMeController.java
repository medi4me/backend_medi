package com.mediforme.mediforme.controller;

import com.mediforme.common.response.ApiResponse;
import com.mediforme.mediforme.config.security.CurrentUserUtils;
import com.mediforme.mediforme.dto.response.UserMeResponseDto;
import com.mediforme.mediforme.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 본인 리소스 전용 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
@Tag(name = "User(Me) API", description = "현재 로그인 사용자 상태 정보 관련 API")
public class UserMeController {
    private final UserService userService;

    /**
     * 내 프로필 조회
     * - 인증된 사용자의 userId 기준 조회
     */
    @Operation(summary = "내 프로필 조회")
    @GetMapping
    public ApiResponse<UserMeResponseDto> getMe() {
        Long userId = CurrentUserUtils.currentUserIdOrThrow();
        return ApiResponse.onSuccess(userService.getMe(userId));
    }
}