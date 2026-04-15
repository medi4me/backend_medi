package com.mediforme.mediforme.check.engine;

import com.mediforme.mediforme.check.port.InteractionRulePort;
import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;
import com.mediforme.mediforme.medicine.dto.MedicineInteractionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 약물 상호작용 계산 엔진
 */
@Component
@RequiredArgsConstructor
public class InteractionEngine {

    private final InteractionRulePort rulePort;

    /**
     * 사용자의 복용 약 목록과 새 복용 예정 약을 비교해 충돌 경고 메시지 리스트를 반환
     */
    public List<String> evaluate(List<MedicineInteractionDto> userMeds, String newMedication) {
        List<String> warnings = new ArrayList<>();

        for (MedicineInteractionDto userMed : userMeds) {
            List<MedicineInteractResponseDto> rules = rulePort.lookupByMedicineName(userMed.getMedicineName());

            for (MedicineInteractResponseDto rule : rules) {
                if (rule.getInteractionWarnings() != null
                    && rule.getInteractionWarnings().contains(newMedication)) {
                    warnings.add(String.format(
                        "%s과(와) %s는 함께 복용하면 안 됩니다! (주의: %s)",
                        userMed.getMedicineName(),
                        newMedication,
                        rule.getInteractionWarnings()));
                }
            }
        }

        return warnings;
    }
}
