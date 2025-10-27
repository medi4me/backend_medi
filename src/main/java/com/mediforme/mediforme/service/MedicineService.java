package com.mediforme.mediforme.service;

import com.mediforme.mediforme.dto.response.OnboardingResponseDto;
import java.io.IOException;
import org.json.simple.parser.ParseException;

public interface MedicineService {
    OnboardingResponseDto getMedicineInfoByName(String itemName) throws IOException, ParseException;    // 약물 API를 통한 약 조회
}
