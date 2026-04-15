package com.mediforme.mediforme.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StatusAdminRequestDto {

    @NotNull(message = "defaultStatusCd는 필수입니다.")
    private Long defaultStatusCd;

    @NotNull(message = "drinkCd는 필수입니다.")
    private Long drinkCd;

    @NotNull(message = "conditionCd는 필수입니다.")
    private Long conditionCd;

    @Size(max = 255, message = "statusMemo는 최대 255자까지 가능합니다.")
    private String statusMemo;

    @NotNull(message = "statusDate는 필수입니다.")
    private LocalDate statusDate;

    @NotNull(message = "userId는 필수입니다.")
    private Long userId;
}
