package com.mediforme.mediforme.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerificationDTO {
    private String phone;
    private String verificationCode;

    public VerificationDTO(String phone, String verificationCode) {
        this.phone = phone;
        this.verificationCode = verificationCode;
    }
}
