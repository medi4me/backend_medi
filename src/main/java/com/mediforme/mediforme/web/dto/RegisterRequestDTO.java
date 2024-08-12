package com.mediforme.mediforme.web.dto;

import com.mediforme.mediforme.domain.enums.MemberConsent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class RegisterRequestDTO {
    @Getter
    @Setter
    public static class JoinDto{
        String name;
        String password;
        String phone;
        String memberID;
        MemberConsent consent;
    }
}
