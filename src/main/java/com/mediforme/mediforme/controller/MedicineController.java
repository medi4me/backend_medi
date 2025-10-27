package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.dto.response.OnboardingResponseDto;
import com.mediforme.mediforme.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/medicine")
public class MedicineController {

    private final MedicineService medicineService;

    @Operation(summary = "의약품 이름으로 검색", description = "약물 공공데이터 API를 통해 약 이름으로 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<OnboardingResponseDto> searchByItemName(@RequestParam String itemName)
            throws IOException, ParseException {
        return ApiResponse.onSuccess(medicineService.getMedicineInfoByName(itemName));
    }
}

