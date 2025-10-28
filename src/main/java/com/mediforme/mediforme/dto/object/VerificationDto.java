package com.mediforme.mediforme.dto.object;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerificationDto {
    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phone;
    private String verificationCode;

    public VerificationDto(String phone, String verificationCode) {
        this.phone = phone;
        this.verificationCode = verificationCode;
    }
}
