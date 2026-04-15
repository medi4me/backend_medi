package com.mediforme.mediforme.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindLoginIdResponseDto {

    @Schema(description = "사용자 로그인 ID")
    private String userLoginId;

    @Schema(description = "사용자 이름")
    private String userName;
}

