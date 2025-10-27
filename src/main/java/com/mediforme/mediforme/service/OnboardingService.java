package com.mediforme.mediforme.service;

import com.mediforme.mediforme.dto.request.OnboardingRequestDto;
import com.mediforme.mediforme.dto.response.OnboardingResponseDto;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public interface OnboardingService {
    // 약 등록, 조회, 삭제
    OnboardingResponseDto saveMedicineInfo(OnboardingRequestDto requestDto) throws IOException, ParseException;
    OnboardingResponseDto getUserMedicines(Long userId);
    void deleteUserMedicine(Long userMedicineId, Long userId);
}
