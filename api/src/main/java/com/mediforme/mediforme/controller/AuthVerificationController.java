package com.mediforme.mediforme.controller;

import com.mediforme.common.response.ApiResponse;
import com.mediforme.mediforme.dto.object.VerificationDto;
import com.mediforme.mediforme.service.PhoneVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/auth/phone")
@Tag(name = "Auth Verification API", description = "휴대폰 인증 API")
public class AuthVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    @Operation(summary = "휴대폰 인증 코드 발송")
    @PostMapping
    public ApiResponse<String> sendCode(@RequestParam String phone) {
        phoneVerificationService.sendCode(phone);
        return ApiResponse.onSuccess("인증 코드가 발송되었습니다.");
    }

    @Operation(summary = "휴대폰 인증 확인")
    @PostMapping("/verify")
    public ApiResponse<String> verify(@RequestBody @Valid VerificationDto request) {
        phoneVerificationService.verify(
                request.getPhone(),
                request.getVerificationCode()
        );
        return ApiResponse.onSuccess("휴대폰 인증이 완료되었습니다.");
    }
}
