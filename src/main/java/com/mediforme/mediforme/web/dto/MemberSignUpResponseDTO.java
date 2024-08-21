package com.mediforme.mediforme.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberSignUpResponseDTO {
    private Long id;
    private String email;
    private String password;
}
