package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.dto.response.MedicineInteractResponseDto;
import com.mediforme.mediforme.service.MedicineInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "약물 성분 충돌 검사", description = "약물 간 상호작용 검사 및 정보 조회")
@RestController
@RequestMapping("/v2/medicine-interactions")
@RequiredArgsConstructor
public class MedicineInteractionsController {
    private final MedicineInteractionService medicineInteractionService;

    // 사용자 복용 중인 약물과 새로 복용하려는 약물 간의 상호작용 검사
    @Operation(summary = "약물 상호작용 검사", description = "사용자 ID와 새 약 이름을 기반으로 복용 중인 약들과의 상호작용 여부를 확인합니다.")
    @PostMapping("/check")
    public ResponseEntity<List<String>> checkDrugInteractions(
            @RequestParam("userId") Long userId,
            @RequestParam("newMedication") String newMedication) {

        try {
            List<String> result = medicineInteractionService.checkDrugInteractions(userId, newMedication);

            if (result.isEmpty()) {
                return ResponseEntity.ok(List.of("상호작용 없음"));
            }
            return ResponseEntity.ok(result);

        } catch (IOException | ParseException e) {
            return ResponseEntity.internalServerError()
                    .body(List.of("상호작용 검사 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // 특정 약 이름으로 상호작용 정보 조회 (관리자 사이트용)
    @Operation(summary = "특정 약물의 상호작용 정보 조회", description = "약물 이름을 입력하면 관련 상호작용 경고 정보를 반환합니다.")
    @GetMapping("/info")
    public ResponseEntity<List<MedicineInteractResponseDto>> getMedicineInteractionInfo(
            @RequestParam("medicineName") String medicineName) {

        List<MedicineInteractResponseDto> result = medicineInteractionService.getMedicineInteractionInfoByName(medicineName);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
}
