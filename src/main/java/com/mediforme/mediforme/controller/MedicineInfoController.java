package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.dto.response.MedicineResponseDto;
import com.mediforme.mediforme.service.MedicineCameraSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.List;

@RestController
public class MedicineInfoController {

    // 카메라 검색 - 인식된 약물 이름을 받아 약물 정보를 조회
    private final MedicineCameraSearchService medicineCameraService;

    @Autowired
    public MedicineInfoController(MedicineCameraSearchService medicineCameraService) {
        this.medicineCameraService = medicineCameraService;
    }

    // 약물 정보 조회
    @GetMapping("/medicine-info")
    public ResponseEntity<List<MedicineResponseDto>> getMedicineInfo(@RequestParam("name") String medicineName) {
        try {
            List<MedicineResponseDto> responseDtoList = medicineCameraService.getMedicineInfoByName(medicineName);

            if (responseDtoList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(responseDtoList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
}
