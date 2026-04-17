package com.mediforme.mediforme.medicine.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedicineSearchItemDto {
    private String name;        // item name
    private String imageUrl;    // item image
    private String source;      // MFDS / FDA / RxNorm
}
