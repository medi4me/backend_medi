package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.dto.request.OnboardingRequestDto;
import com.mediforme.mediforme.dto.response.OnboardingResponseDto;
import com.mediforme.mediforme.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "사용자 복용 약 등록", description = "사용자가 복용할 약을 등록합니다.")
    @PostMapping("/register")
    public ApiResponse<OnboardingResponseDto> register(@RequestBody OnboardingRequestDto request)
            throws IOException, ParseException {
        return ApiResponse.onSuccess(onboardingService.saveMedicineInfo(request));
    }

    @Operation(summary = "사용자 복용 약 목록 조회")
    @GetMapping("/list/{userId}")
    public ApiResponse<OnboardingResponseDto> getUserMedicines(@PathVariable Long userId) {
        return ApiResponse.onSuccess(onboardingService.getUserMedicines(userId));
    }

    @Operation(summary = "사용자 복용 약 삭제")
    @DeleteMapping("/{userMedicineId}/delete")
    public ApiResponse<String> deleteUserMedicine(@PathVariable Long userMedicineId,
                                                  @RequestParam Long userId) {
        onboardingService.deleteUserMedicine(userMedicineId, userId);
        return ApiResponse.onSuccess("복용 약이 삭제되었습니다.");
    }
}
