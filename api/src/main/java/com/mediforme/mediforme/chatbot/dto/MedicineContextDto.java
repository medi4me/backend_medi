package com.mediforme.mediforme.chatbot.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineContextDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 200)
    private String nameKo;

    @Size(max = 200)
    private String ingredientKo;

    @Size(max = 20)
    private String rxOtc;

    @Size(max = 1000)
    private String indicationsSummary;

    @Size(max = 1000)
    private String warningsSummary;

    public boolean hasAny() {
        return isPresent(nameKo) || isPresent(ingredientKo) || isPresent(rxOtc)
                || isPresent(indicationsSummary) || isPresent(warningsSummary);
    }

    private static boolean isPresent(String v) {
        return v != null && !v.isBlank();
    }
}
