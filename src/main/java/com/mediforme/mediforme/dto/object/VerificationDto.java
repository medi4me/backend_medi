package com.mediforme.mediforme.dto.object;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerificationDto {
    private String phone;
    private String verificationCode;

    public VerificationDto(String phone, String verificationCode) {
        this.phone = phone;
        this.verificationCode = verificationCode;
    }
}
