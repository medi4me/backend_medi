package com.mediforme.mediforme.check.engine;

import com.mediforme.mediforme.check.port.InteractionRulePort;
import com.mediforme.mediforme.medicine.dto.MedicineInteractionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 약물 상호작용 계산 엔진
 *
 * 새 복용 예정 약의 병용금기 성분 집합을 한 번 조회한 뒤, 기존 복용 약의 성분(component)과
 * 정규화 비교한다. 충돌 시 DUR `PROHBT_CONTENT`(사유)를 경고 문구에 함께 부착한다.
 */
@Component
@RequiredArgsConstructor
public class InteractionEngine {

    private final InteractionRulePort rulePort;

    /**
     * 사용자의 복용 약 목록과 새 복용 예정 약을 비교해 충돌 경고 메시지 리스트를 반환
     */
    public List<String> evaluate(List<MedicineInteractionDto> userMeds, String newMedication) {
        if (userMeds == null || userMeds.isEmpty()) {
            return List.of();
        }

        Map<String, String> reasons = rulePort.lookupContraindicatedIngredients(newMedication);
        if (reasons.isEmpty()) {
            return List.of();
        }

        // 정규화된 키 ↔ 원본 키·사유를 미리 묶어 둔다
        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<String, String> e : reasons.entrySet()) {
            String norm = normalize(e.getKey());
            if (!norm.isBlank()) {
                entries.add(new Entry(norm, e.getKey(), e.getValue() == null ? "" : e.getValue()));
            }
        }
        if (entries.isEmpty()) {
            return List.of();
        }

        List<String> warnings = new ArrayList<>();
        for (MedicineInteractionDto userMed : userMeds) {
            String candidate = hasText(userMed.getComponent())
                ? userMed.getComponent() : userMed.getMedicineName();
            if (!hasText(candidate)) {
                continue;
            }
            String norm = normalize(candidate);
            Entry match = entries.stream()
                .filter(e -> norm.contains(e.norm) || e.norm.contains(norm))
                .findFirst()
                .orElse(null);
            if (match != null) {
                String reasonSuffix = hasText(match.reason) ? "(" + match.reason + ")" : "";
                warnings.add(String.format(
                    "%s과(와) %s는 병용금기입니다%s. 함께 복용 전 의사·약사와 상담하세요.",
                    userMed.getMedicineName(), newMedication, reasonSuffix));
            }
        }
        return warnings;
    }

    private record Entry(String norm, String original, String reason) {}

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("\\s+", "");
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
