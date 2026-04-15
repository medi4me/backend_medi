package com.mediforme.mediforme.controller;

import com.mediforme.common.response.ApiResponse;
import com.mediforme.mediforme.dto.response.MedicineCameraResponseDto;
import com.mediforme.mediforme.dto.response.MedicineSearchResponseDto;
import com.mediforme.mediforme.dto.response.OnboardingResponseDto;
import com.mediforme.mediforme.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Medicines", description = "의약품 검색/조회")
@RequestMapping("/medicines")
public class MedicinesController {

    private final MedicineService medicineService;

    @Operation(summary = "의약품 이름으로 검색(온보딩)", description = "약물 공공데이터 API를 통해 약 이름으로 검색합니다.")
    @GetMapping
    public ApiResponse<MedicineSearchResponseDto> searchByItemName(@RequestParam("name") @NotBlank String name)
        throws IOException, ParseException {
        return ApiResponse.onSuccess(medicineService.getMedicineInfoByName(name));
    }

    @Operation(summary = "약물명으로 상세 정보 조회", description = "공공 약물 데이터 API를 통해 약물명을 기반으로 약 상세 정보를 조회합니다.")
    @GetMapping("/info")
    public ApiResponse<List<MedicineCameraResponseDto.MedicineInfoDto>> getMedicineInfo(
        @RequestParam("name") @NotBlank String name
    ) throws IOException, ParseException {
        return ApiResponse.onSuccess(medicineService.getMedicineInfoSimple(name));
    }
}

