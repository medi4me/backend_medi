package com.mediforme.mediforme.controller;

import com.mediforme.common.response.ApiResponse;
import com.mediforme.mediforme.config.security.CurrentUserUtils;
import com.mediforme.mediforme.dto.request.OnboardingRequestDto;
import com.mediforme.mediforme.dto.response.OnboardingResponseDto;
import com.mediforme.mediforme.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 내 복용 약(온보딩) 리소스 용도
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/medicines")
public class OnboardingMeController {

    private final OnboardingService onboardingService;

    @Operation(summary = "내 복용 약 등록", description = "현재 로그인 사용자의 복용 약을 등록합니다.")
    @PostMapping
    public ApiResponse<OnboardingResponseDto> register(@RequestBody OnboardingRequestDto request)
            throws IOException, ParseException {
        Long userId = CurrentUserUtils.currentUserIdOrThrow();
        return ApiResponse.onSuccess(onboardingService.saveMedicineInfo(userId, request));
    }

    @Operation(summary = "내 복용 약 목록 조회", description = "현재 로그인 사용자의 복용 약 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<OnboardingResponseDto> getMyMedicines() {
        Long userId = CurrentUserUtils.currentUserIdOrThrow();
        return ApiResponse.onSuccess(onboardingService.getUserMedicines(userId));
    }

    @Operation(summary = "내 복용 약 삭제", description = "현재 로그인 사용자의 복용 약을 삭제합니다.")
    @DeleteMapping("/{userMedicineId}")
    public ApiResponse<String> deleteMyMedicine(@PathVariable Long userMedicineId) {
        Long userId = CurrentUserUtils.currentUserIdOrThrow();
        onboardingService.deleteUserMedicine(userMedicineId, userId);
        return ApiResponse.onSuccess("복용 약이 삭제되었습니다.");
    }
}
