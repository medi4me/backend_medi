package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.dto.response.MedicineResponseDto;
import com.mediforme.mediforme.service.MedicineCameraSearchService;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController

public class RecognizeImageController {
    // 카메라 검색 - 이미지를 통해 약물 이름을 인식
    private final MedicineCameraSearchService medicineCameraService;

    @Autowired
    public RecognizeImageController(MedicineCameraSearchService medicineCameraService) {
        this.medicineCameraService = medicineCameraService;
    }

    // 카메라 약물 인식
    @PostMapping("/camera")
    public ResponseEntity<List<MedicineResponseDto>> recognizeImage(@RequestParam("file") MultipartFile file) throws IOException, ParseException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 이미지 처리 및 약물 정보 조회
            List<MedicineResponseDto> responseDtoList = medicineCameraService.processImageAndRecognizeMedicine(file);

            // 약물 정보를 추출하여 응답
            if (responseDtoList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(responseDtoList);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
