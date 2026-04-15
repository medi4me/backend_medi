package com.mediforme.mediforme.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPasswordResponseDto {

    @Schema(description = "비밀번호 재설정 안내 메시지", example = "비밀번호 재설정 링크를 발송했습니다.")
    private String message;

    @Schema(description = "임시 비밀번호 또는 재설정용 토큰", example = "resetToken-83FJ39SK")
    private String resetToken;
}
