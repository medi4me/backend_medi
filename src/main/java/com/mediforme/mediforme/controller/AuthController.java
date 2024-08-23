package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.config.jwt.JwtTokenProvider;
import com.mediforme.mediforme.dto.request.MemberRequestDTO;
import com.mediforme.mediforme.dto.response.MemberLoginResponseDTO;
import com.mediforme.mediforme.service.MemberService;
import com.mediforme.mediforme.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "로그인 API")
    @PostMapping("/login")
    public ApiResponse<MemberLoginResponseDTO> login(@RequestBody MemberRequestDTO.LoginRequestDto request) {
        return ApiResponse.onSuccess(memberService.login(request));
    }

    @Operation(summary = "로그아웃 API")
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        // Authorization 헤더에서 토큰 추출
        String token = jwtTokenProvider.parseBearerToken(request);

        if (token != null) {
            // 토큰 블랙리스트에 추가
            tokenBlacklistService.addToBlacklist(token);
            return ApiResponse.onSuccess("Successfully logged out.");
        } else {
            return ApiResponse.onFailure("INVALID_TOKEN", "Token is missing or invalid.", null);
        }
    }

    @GetMapping("/search-name/{memberID}")
    public ApiResponse<String> getMemberName(@PathVariable("memberID") String memberID) {
        String memberName = memberService.findMemberNameByID(memberID);
        return ApiResponse.onSuccess(memberName);
    }
}
