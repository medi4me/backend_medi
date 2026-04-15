package com.mediforme.mediforme.medicine.service;

import com.mediforme.mediforme.medicine.dto.MedicineInteractionDto;
import com.mediforme.mediforme.medicine.dto.MedicineCameraResponseDto;
import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;

public interface MedicineService {
    List<MedicineCameraResponseDto.MedicineInfoDto> getMedicineInfoSimple(String itemName) throws IOException, ParseException;  // (카메라 약물 인식) 약물 API를 통한 약 간단 조회
    List<MedicineInteractionDto> getUserMedicineSummaries(Long userId);                                                         // 사용자 복용 약 요약
}
