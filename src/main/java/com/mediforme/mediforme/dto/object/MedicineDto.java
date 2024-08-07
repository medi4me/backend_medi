package com.mediforme.mediforme.dto.object;

import com.mediforme.mediforme.domain.Medicine;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MedicineDto {

    private Long id;
    private String name;
    private String description;
    private String benefit;
    private String drugInteraction;
    private String component;
    private Integer amount;

}
