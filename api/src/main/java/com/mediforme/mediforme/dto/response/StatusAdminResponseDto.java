package com.mediforme.mediforme.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StatusAdminResponseDto {
    private Long statusId;
    private Long defaultStatusCd;
    private Long drinkCd;
    private Long conditionCd;
    private String statusMemo;
    private LocalDate statusDate;
    private Long userId;

    // 공통코드명
    private String defaultStatusName;
    private String drinkName;
    private String conditionName;
}
