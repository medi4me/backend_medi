package com.mediforme.mediforme.web.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

public class LoginRequestDTO {
    @Getter
    @Setter
    public static class LoginDto {
        @NotEmpty(message = "Username cannot be empty")
        private String memberID;

        @NotEmpty(message = "Password cannot be empty")
        private String password;
    }
}
