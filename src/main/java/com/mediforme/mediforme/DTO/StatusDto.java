package com.mediforme.mediforme.DTO;

import com.mediforme.mediforme.domain.enums.StatusCondition;
import com.mediforme.mediforme.domain.enums.StatusDrink;
import com.mediforme.mediforme.domain.enums.StatusStatus;
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