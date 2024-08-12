package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.service.LoginService;
import com.mediforme.mediforme.web.dto.LoginRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody @Valid LoginRequestDTO.LoginDto request) {
        boolean isAuthenticated = loginService.authenticate(request.getMemberID(), request.getPassword());

        if (isAuthenticated) {
            return ApiResponse.onSuccess("Login successful.");
        } else {
            return ApiResponse.onFailure("LOGIN_FAILED", "Invalid email or password.", null);
        }
    }
}
