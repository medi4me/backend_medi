package com.mediforme.mediforme.DTO;

import com.mediforme.mediforme.domain.enums.StatusCondition;
import com.mediforme.mediforme.domain.enums.StatusDrink;
import com.mediforme.mediforme.domain.enums.StatusStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;


@Getter
@Setter
public class StatusDto {

    @NotNull
    private StatusStatus status;

    @NotNull
    private StatusDrink drink;

    @NotNull
    private StatusCondition condition;

    @Size(max = 255)
    private String memo;
}
