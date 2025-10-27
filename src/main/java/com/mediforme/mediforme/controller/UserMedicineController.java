package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.service.UserMedicineService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/user-medicine")
public class UserMedicineController {

    private final UserMedicineService userMedicineService;

    @Operation(summary = "약 복용 체크")
    @PutMapping("/{userMedicineId}/check")
    public ApiResponse<String> checkMedicine(@PathVariable Long userMedicineId) {
        userMedicineService.checkMedicine(userMedicineId);
        return ApiResponse.onSuccess("복용 완료 처리되었습니다.");
    }

    @Operation(summary = "약 복용 체크 해제")
    @PutMapping("/{userMedicineId}/uncheck")
    public ApiResponse<String> uncheckMedicine(@PathVariable Long userMedicineId) {
        userMedicineService.uncheckMedicine(userMedicineId);
        return ApiResponse.onSuccess("복용 상태가 해제되었습니다.");
    }

    @Operation(summary = "약 알람 설정/해제")
    @PutMapping("/{userMedicineId}/alarm")
    public ApiResponse<String> toggleAlarm(@PathVariable Long userMedicineId,
                                           @RequestParam boolean isOn) {
        userMedicineService.toggleAlarm(userMedicineId, isOn);
        return ApiResponse.onSuccess(isOn ? "알람이 켜졌습니다." : "알람이 꺼졌습니다.");
    }
}
