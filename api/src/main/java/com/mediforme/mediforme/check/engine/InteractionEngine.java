package com.mediforme.mediforme.check.engine;

import com.mediforme.mediforme.check.port.InteractionRulePort;
import com.mediforme.mediforme.medicine.dto.MedicineInteractionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 약물 상호작용 계산 엔진
 *
 * 새 복용 예정 약의 병용금기 성분 집합을 한 번 조회한 뒤, 기존 복용 약의 성분(component)과
 * 정규화 비교한다. 이름이 아닌 성분 기준으로 매칭해 제품명·브랜드·용량 표기 차이를 흡수한다.
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

        Set<String> contraindicated = rulePort.lookupContraindicatedIngredients(newMedication).stream()
            .map(InteractionEngine::normalize)
            .filter(s -> !s.isBlank())
            .collect(Collectors.toSet());
        if (contraindicated.isEmpty()) {
            return List.of();
        }

        List<String> warnings = new ArrayList<>();
        for (MedicineInteractionDto userMed : userMeds) {
            // 기존 약은 성분(component)을 우선 사용, 없으면 약 이름으로 대체
            String candidate = hasText(userMed.getComponent())
                ? userMed.getComponent() : userMed.getMedicineName();
            if (!hasText(candidate)) {
                continue;
            }
            String norm = normalize(candidate);
            boolean conflict = contraindicated.stream()
                .anyMatch(c -> norm.contains(c) || c.contains(norm));
            if (conflict) {
                warnings.add(String.format(
                    "%s과(와) %s는 병용금기입니다. 함께 복용 전 의사·약사와 상담하세요.",
                    userMed.getMedicineName(), newMedication));
            }
        }
        return warnings;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("\\s+", "");
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
