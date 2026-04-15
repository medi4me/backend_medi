package com.mediforme.mediforme.dto.object;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StatusDto {

    @NotNull
    private Long defaultStatusCd;    // 기본 상태 코드 (좋음/보통/나쁨)

    @NotNull
    private Long drinkCd;            // 음주 상태 코드

    @NotNull
    private Long conditionCd;        // 컨디션 코드 (피곤/상쾌 등)

    @Size(max = 255)
    private String statusMemo;

    @NotNull
    private LocalDate statusDate;

    private Long userId;             // 사용자 ID

    // 공통코드명 표시 용도
    private String defaultStatusName;
    private String drinkName;
    private String conditionName;
}
