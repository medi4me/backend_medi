package com.mediforme.mediforme.converter;

import com.mediforme.mediforme.domain.Medicine;
import com.mediforme.mediforme.dto.object.MedicineDto;
import org.springframework.stereotype.Component;

@Component
public class MedicineConverter {
    public MedicineDto toDto(Medicine medicine) {
        // 약 도메인 객체를 DTO로 변환
        return MedicineDto.builder()
                .id(medicine.getId()).
                name(medicine.getName())
                .description(medicine.getDescription())
                .benefit(medicine.getBenefit())
                .drugInteraction(medicine.getDrugInteraction())
                .component(medicine.getComponent())
                .amount(medicine.getAmount())
                .build();
    }

    public Medicine toEntity(MedicineDto medicineDto) {
        // DTO를 도메인 객체로 변환
        return Medicine.builder()
                .id(medicineDto.getId())
                .name(medicineDto.getName())
                .description(medicineDto.getDescription())
                .benefit(medicineDto.getBenefit())
                .drugInteraction(medicineDto.getDrugInteraction())
                .component(medicineDto.getComponent())
                .amount(medicineDto.getAmount())
                .build();
    }
}
