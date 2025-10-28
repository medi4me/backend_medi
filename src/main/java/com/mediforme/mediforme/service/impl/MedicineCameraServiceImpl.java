package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.dto.response.MedicineCameraResponseDto;
import com.mediforme.mediforme.service.MedicineService;
import com.mediforme.mediforme.service.MedicineCameraService;
import com.mediforme.mediforme.service.MedicineImageRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.json.simple.parser.ParseException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineCameraServiceImpl implements MedicineCameraService {

    private final MedicineService medicineService;
    private final MedicineImageRecognitionService recognitionService;

    @Override
    public MedicineCameraResponseDto processImage(MultipartFile file) throws IOException, ParseException {
        // Vision API로 약 이름 인식
        String recognizedName = recognitionService.recognizeMedicineName(file);

        if (recognizedName == null || recognizedName.isEmpty()) {
            return MedicineCameraResponseDto.builder()
                    .recognizedText("인식 실패")
                    .recognizedMedicine(null)
                    .medicineInfo(Collections.emptyList())
                    .build();
        }

        // 공공 약물 데이터 API로 약 정보 조회
        List<MedicineCameraResponseDto.MedicineInfoDto> info = medicineService.getMedicineInfoSimple(recognizedName);

        return MedicineCameraResponseDto.builder()
                .recognizedText(recognizedName)
                .recognizedMedicine(recognizedName)
                .medicineInfo(info)
                .build();
    }
}
