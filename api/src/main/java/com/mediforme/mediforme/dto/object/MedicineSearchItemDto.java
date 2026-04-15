package com.mediforme.mediforme.dto.object;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedicineSearchItemDto {
    private String name;        // item name
    private String imageUrl;    // item image
}
