package com.mediforme.mediforme.check.application;

import com.mediforme.mediforme.check.engine.InteractionEngine;
import com.mediforme.mediforme.check.event.CheckCompletedEvent;
import com.mediforme.mediforme.check.port.InteractionRulePort;
import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;
import com.mediforme.mediforme.medicine.dto.MedicineInteractionDto;
import com.mediforme.mediforme.medicine.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 약물 상호작용 검사 유스케이스
 */
@Service
@RequiredArgsConstructor
public class InteractionCheckService {

    private final InteractionEngine engine;
    private final InteractionRulePort rulePort;
    private final MedicineService medicineService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 사용자가 새로 복용하려는 약과 기존 복용 약들 사이의 상호작용 검사
     */
    public List<String> check(Long userId, String newMedication) {
        List<MedicineInteractionDto> userMeds = medicineService.getUserMedicineSummaries(userId);
        List<String> warnings = engine.evaluate(userMeds, newMedication);

        eventPublisher.publishEvent(new CheckCompletedEvent(
            userId,
            newMedication,
            warnings,
            Instant.now()
        ));

        return warnings;
    }

    /**
     * 특정 약 이름의 상호작용 규칙을 조회 (/info 엔드포인트용)
     */
    public List<MedicineInteractResponseDto> lookupRules(String medicineName) {
        return rulePort.lookupByMedicineName(medicineName);
    }
}
