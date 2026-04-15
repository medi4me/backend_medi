package com.mediforme.mediforme.check.adapter;

import com.mediforme.mediforme.check.port.InteractionRulePort;
import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link InteractionRulePort} 의 임시 Mock 구현
 */
@Component
public class MockInteractionRuleAdapter implements InteractionRulePort {

    @Override
    public List<MedicineInteractResponseDto> lookupByMedicineName(String medicineName) {
        List<MedicineInteractResponseDto> list = new ArrayList<>();

        if ("타이레놀".equalsIgnoreCase(medicineName)) {
            list.add(MedicineInteractResponseDto.builder()
                .name("타이레놀")
                .interactionWarnings("아세트아미노펜, 와파린")
                .build());
        }

        if ("이부프로펜".equalsIgnoreCase(medicineName)) {
            list.add(MedicineInteractResponseDto.builder()
                .name("이부프로펜")
                .interactionWarnings("아스피린, 와파린")
                .build());
        }

        return list;
    }
}
