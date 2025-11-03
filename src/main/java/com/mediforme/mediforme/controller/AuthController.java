package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.config.security.jwt.JwtToken;
import com.mediforme.mediforme.config.security.jwt.JwtTokenProvider;
import com.mediforme.mediforme.dto.request.UserLoginRequestDto;
import com.mediforme.mediforme.dto.request.UserRegisterRequestDto;
import com.mediforme.mediforme.dto.response.UserLoginResponseDto;
import com.mediforme.mediforme.service.AuthService;
import com.mediforme.mediforme.service.UserService;
import com.mediforme.mediforme.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/users/auth")
@Tag(name = "Auth API", description = "로그인, 로그아웃 및 토큰 재발급 API")
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthService authService;

    @Operation(summary = "로그인", description = "사용자 로그인 Id와 비밀번호를 검증하고 JWT 토큰을 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<UserLoginResponseDto> login(@RequestBody @Valid UserLoginRequestDto.LoginRequestDto request) {
        UserLoginResponseDto response = authService.login(request);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null) {
            return ApiResponse.onFailure("INVALID_TOKEN", "유효하지 않은 토큰이거나 존재하지 않습니다.", null);
        }
        tokenBlacklistService.addToBlacklist(token);
        return ApiResponse.onSuccess("로그아웃이 성공적으로 처리되었습니다.");
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 검증하고 새로운 Access Token과 Refresh Token을 발급합니다.")
    @PostMapping("/reissue")
    public ApiResponse<JwtToken> reissue(@RequestHeader("Authorization") String refreshHeader) {
        if (refreshHeader == null || !refreshHeader.startsWith("Bearer ")) {
            return ApiResponse.onFailure("INVALID_TOKEN", "Refresh Token이 유효하지 않습니다.", null);
        }

        String refreshToken = refreshHeader.substring(7);
        JwtToken newToken = authService.reissue(refreshToken);
        return ApiResponse.onSuccess(newToken);
    }


    @Operation(summary = "사용자 이름 조회", description = "로그인 Id를 기준으로 사용자 이름을 조회합니다.")
    @GetMapping("/search-name/{userLoginId}")
    public ApiResponse<String> getUserName(@PathVariable("userLoginId") String userLoginId) {
        String userName = authService.getLoginUser().getUserName();
        return ApiResponse.onSuccess(userName);
    }
}
