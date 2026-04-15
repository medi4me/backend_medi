package com.mediforme.mediforme.check.port;

import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;

import java.util.List;

/**
 * 약 이름을 기반 해당 약의 상호작용 규칙/경고 조회 포트
 */
public interface InteractionRulePort {

    /**
     * 주어진 약 이름에 대한 상호작용 규칙 조회
     */
    List<MedicineInteractResponseDto> lookupByMedicineName(String medicineName);
}
