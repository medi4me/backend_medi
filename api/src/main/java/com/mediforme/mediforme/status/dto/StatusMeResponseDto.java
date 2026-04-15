package com.mediforme.mediforme.status.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StatusMeResponseDto {

    private Long statusId;
    private LocalDate statusDate;
    private Long defaultStatusCd;
    private Long drinkCd;
    private Long conditionCd;
    private String statusMemo;

    private String defaultStatusName;
    private String drinkName;
    private String conditionName;
}

