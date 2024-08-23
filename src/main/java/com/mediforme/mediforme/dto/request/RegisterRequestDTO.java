package com.mediforme.mediforme.dto.request;

import com.mediforme.mediforme.domain.enums.MemberConsent;
import lombok.Getter;
import lombok.Setter;

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
