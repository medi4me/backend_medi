package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.config.jwt.JwtAuthenticationFilter;
import com.mediforme.mediforme.config.jwt.JwtTokenProvider;
import com.mediforme.mediforme.service.AuthService;
import com.mediforme.mediforme.service.ResignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.TableGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/users")
@Tag(name = "회훤 탈퇴", description = "회원을 탈퇴시킵니다.(탈퇴 상태 처리)")
public class ResignController {
    private final AuthService authService;
    private final ResignService resignService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Operation(summary = "회원 탈퇴", description = "JWT 토큰을 기반으로 현재 로그인한 사용자를 탈퇴 처리합니다.")
    @DeleteMapping("/resigh")
    public ApiResponse<String> resign(HttpServletRequest request) {
        try {
            // Authorization 헤더에서 Bearer 토큰 추출
            String token = jwtTokenProvider.parseBearerToken(request);

            if (token == null || ! jwtTokenProvider.validateToken(token)){
                throw new CustomApiException(ErrorCode.INVALID_JWT_TOKEN);
            }

            // 현재 로그인한 사용자 정보 가져오기
            String loginId = authService.getLoginUserLoginId();

            // 회원 탈퇴 처리
            resignService.resignUser(loginId);
            return ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다.");

        } catch (CustomApiException e) {
            return ApiResponse.onFailure(e.getErrorCode().name(), e.getMessage(), null);
        } catch (Exception e) {
            return ApiResponse.onFailure("INTERNAL_ERROR", "회원 탈퇴 중 오류가 발생했습니다.", null);
        }
    }
}