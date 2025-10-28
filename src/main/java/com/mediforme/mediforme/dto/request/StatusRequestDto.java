package com.mediforme.mediforme.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StatusRequestDto {

    @NotNull
    private Long defaultStatusCd;

    @NotNull
    private Long drinkCd;

    @NotNull
    private Long conditionCd;

    @Size(max = 255)
    private String statusMemo;

    @NotNull
    private LocalDate statusDate;

    private Long userId;
}
