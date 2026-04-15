package com.mediforme.mediforme.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDto {

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    private String phone;

    @NotBlank(message = "비밀번호 재설정 토큰은 필수입니다.")
    private String token;

    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    private String newPassword;
}
