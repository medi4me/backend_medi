package com.mediforme.mediforme.user.controller;

import com.mediforme.common.response.ApiResponse;
import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.global.security.jwt.JwtToken;
import com.mediforme.mediforme.user.dto.UserLoginRequestDto;
import com.mediforme.mediforme.user.dto.UserRegisterRequestDto;
import com.mediforme.mediforme.user.dto.UserLoginResponseDto;
import com.mediforme.mediforme.user.service.AuthService;
import com.mediforme.mediforme.global.security.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth API", description = "로그인, 로그아웃 및 토큰 재발급 API")
public class AuthController {
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthService authService;

    @Operation(summary = "로그인", description = "사용자 로그인 Id와 비밀번호를 검증하고 JWT 토큰을 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<UserLoginResponseDto> login(
            @RequestBody @Valid UserLoginRequestDto.LoginRequestDto request) {
        return ApiResponse.onSuccess(authService.login(request));
    }

    @Operation(summary = "아이디 중복 확인")
    @GetMapping("/check-id")
    public ApiResponse<String> checkUserLoginId(@RequestParam String userLoginId) {
        authService.checkDuplicateLoginId(userLoginId);
        return ApiResponse.onSuccess("사용 가능한 아이디입니다.");
    }

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ApiResponse<UserLoginResponseDto> register(
            @RequestBody @Valid UserRegisterRequestDto.JoinRequest request
    ) {
        return ApiResponse.onSuccess(authService.register(request));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        String accessToken = extractBearerToken(request);
        tokenBlacklistService.addToBlacklist(accessToken);
        return ApiResponse.onSuccess("로그아웃이 성공적으로 처리되었습니다.");
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 검증하고 새로운 Access Token과 Refresh Token을 발급합니다.")
    @PostMapping("/reissue")
    public ApiResponse<JwtToken> reissue(@RequestHeader("Authorization") String refreshHeader) {
        String refreshToken = extractBearerToken(refreshHeader);
        return ApiResponse.onSuccess(authService.reissue(refreshToken));
    }

    // 공통 토큰 파싱
    private String extractBearerToken(HttpServletRequest request) {
        return extractBearerToken(request.getHeader("Authorization"));
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomApiException(ErrorCode.AUTH_EMPTY_JWT_CLAIMS);
        }
        return authorizationHeader.substring(7);
    }
}
