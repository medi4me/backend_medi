package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.dto.response.MedicineCameraResponseDto;
import com.mediforme.mediforme.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "Medicine Info", description = "공공 약물 데이터 API 기반 약물 정보 조회")
@RestController
@RequestMapping("/v2/medicine")
@RequiredArgsConstructor
public class MedicineInfoController {

    private final MedicineService medicineService;

    @Operation(summary = "약물명으로 정보 조회", description = "공공 약물 데이터 API를 통해 약물명을 기반으로 약 정보를 조회합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<MedicineCameraResponseDto.MedicineInfoDto>> getMedicineInfo(
            @RequestParam("name") String medicineName) {

        try {
            List<MedicineCameraResponseDto.MedicineInfoDto> result = medicineService.getMedicineInfoSimple(medicineName);

            if (result.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}