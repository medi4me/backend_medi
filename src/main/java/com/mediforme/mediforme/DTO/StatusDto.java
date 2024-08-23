package com.mediforme.mediforme.dto;

import com.mediforme.mediforme.domain.enums.StatusCondition;
import com.mediforme.mediforme.domain.enums.StatusDrink;
import com.mediforme.mediforme.domain.enums.StatusStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;


@Getter
@Setter
public class StatusDto {

    @NotNull
    private StatusStatus status;

    @NotNull
    private StatusDrink drink;

    @NotNull
    private StatusCondition statusCondition;

    @Size(max = 255)
    private String memo;

    @NotNull
    private LocalDate date; //캘린더
}