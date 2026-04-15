package com.mediforme.mediforme.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StatusMeRequestDto {

    @NotNull
    private LocalDate statusDate;

    @NotNull
    private Long defaultStatusCd;

    private Long drinkCd;
    private Long conditionCd;
    private String statusMemo;
}

