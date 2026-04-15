package com.mediforme.mediforme.check.service;

import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;
import com.mediforme.mediforme.user.dto.OnboardingDto;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public interface MedicineInteractionService {
    // 사용자 id와 새로 복용하려는 약물 이름을 기준으로 기존 복용중인 약들과의 성분 충돌 검사
    List<String> checkDrugInteractions(Long userId, String newMedication) throws IOException, ParseException;

    // 약 이름을 통한 상호작용 정보 조회
    List<MedicineInteractResponseDto> getMedicineInteractionInfoByName(String medicineName);
}
