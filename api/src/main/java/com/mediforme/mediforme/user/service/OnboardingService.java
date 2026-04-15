package com.mediforme.mediforme.user.service;

import com.mediforme.mediforme.user.dto.OnboardingRequestDto;
import com.mediforme.mediforme.user.dto.OnboardingResponseDto;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public interface OnboardingService {
    // 약 등록, 조회, 삭제
    OnboardingResponseDto saveMedicineInfo(Long userId, OnboardingRequestDto requestDto) throws IOException, ParseException;
    OnboardingResponseDto getUserMedicines(Long userId);
    void deleteUserMedicine(Long userMedicineId, Long userId);
}
