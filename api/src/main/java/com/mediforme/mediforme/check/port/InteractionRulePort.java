package com.mediforme.mediforme.check.port;

import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;

import java.util.List;
import java.util.Map;

/**
 * 약 이름을 기반 해당 약의 상호작용 규칙/경고 조회 포트
 */
public interface InteractionRulePort {

    /**
     * 주어진 약 이름에 대한 상호작용 규칙 조회 (/info 표시용)
     */
    List<MedicineInteractResponseDto> lookupByMedicineName(String medicineName);

    /**
     * 주어진 약과 병용금기인 상대 성분명 → 사유 매핑 조회 (성분 충돌 검사용)
     * 키는 상대 성분명, 값은 병용금기 사유(`PROHBT_CONTENT`). 사유가 비어 있으면 빈 문자열
     */
    Map<String, String> lookupContraindicatedIngredients(String medicineName);
}
