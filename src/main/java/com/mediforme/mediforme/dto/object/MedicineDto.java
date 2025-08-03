package com.mediforme.mediforme.dto.object;

import com.mediforme.mediforme.domain.Medicine;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicineDto {

    private Long id;
    private String name;
    private String description;
    private String benefit;
    private String drugInteraction;
    private String component;
    private Integer amount;

    public static MedicineDto fromEntity(Medicine medicine){
        // 약 도메인 객체를 DTO로 변환
        return MedicineDto.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .description(medicine.getDescription())
                .benefit(medicine.getBenefit())
                .drugInteraction(medicine.getDrugInteraction())
                //.component(medicine.getComponent())
                //.amount(medicine.getAmount())
                .build();

    }


    public Medicine toEntity() {
        // DTO를 도메인 객체로 변환
        return Medicine.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .benefit(this.benefit)
                .drugInteraction(this.drugInteraction)
                //.component(this.component)
                //.amount(this.amount)
                .build();
    }
}
