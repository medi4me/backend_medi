package com.mediforme.mediforme.check.controller;

import com.mediforme.common.response.ApiResponse;
import com.mediforme.mediforme.check.application.InteractionCheckService;
import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "약물 성분 충돌 검사", description = "약물 간 상호작용 검사 및 정보 조회")
@RestController
@RequestMapping("/v2/medicine-interactions")
@RequiredArgsConstructor
public class MedicineInteractionsController {

    private final InteractionCheckService interactionCheckService;

    // 사용자 복용 중인 약물과 새로 복용하려는 약물 간의 상호작용 검사
    @Operation(summary = "약물 상호작용 검사", description = "사용자 ID와 새 약 이름을 기반으로 복용 중인 약들과의 상호작용 여부를 확인합니다.")
    @PostMapping("/check")
    public ApiResponse<List<String>> checkDrugInteractions(
            @RequestParam("userId") Long userId,
            @RequestParam("newMedication") String newMedication) {

        List<String> result = interactionCheckService.check(userId, newMedication);

        if (result.isEmpty()) {
            return ApiResponse.onSuccess(List.of("상호작용 없음"));
        }
        return ApiResponse.onSuccess(result);
    }

    // 특정 약 이름으로 상호작용 정보 조회 (관리자 사이트용)
    @Operation(summary = "특정 약물의 상호작용 정보 조회", description = "약물 이름을 입력하면 관련 상호작용 경고 정보를 반환합니다.")
    @GetMapping("/info")
    public ApiResponse<List<MedicineInteractResponseDto>> getMedicineInteractionInfo(
            @RequestParam("medicineName") String medicineName) {

        return ApiResponse.onSuccess(interactionCheckService.lookupRules(medicineName));
    }
}
