package com.mediforme.mediforme.config.security;

import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.config.security.jwt.SecurityTokenAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// 현재 요청 보낸 로그인 사용자 정보를 SecurityContext에서 조회
public final class CurrentUserUtils {

    private CurrentUserUtils() {}

    /**
     * 현재 로그인한 사용자의 CustomUserDetails 반환
     * @return CustomUserDetails (userId, loginId, role, status 포함)
     * @throws CustomApiException 인증 정보가 없거나 인증되지 않은 경우
     */
    public static CustomUserDetails currentUserDetailsOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보 자체가 없는 경우 (필터를 거치지 않았거나, 인증 실패)
        if (auth == null || auth.getPrincipal() == null) {
            throw new CustomApiException(ErrorCode.COMMON_UNAUTHORIZED);
        }

        Object principal = auth.getPrincipal();

        // 인증은 되었으나 principal 타입이 기대한 타입이 아닌 경우
        if (!(principal instanceof CustomUserDetails cud)) {
            // principal이 익명 유저거나 다른 타입인 경우
            throw new CustomApiException(ErrorCode.COMMON_UNAUTHORIZED);
        }
        return cud;
    }


    /**
     * 현재 로그인한 사용자의 userId 반환 (내 정보 조회/수정 API에서 사용)
     * @return userId (DB PK)
     * @throws CustomApiException 인증되지 않았거나 userId가 없는 경우
     */
    public static Long currentUserIdOrThrow() {
        Long userId = currentUserDetailsOrThrow().getUserId();
        if (userId == null) {
            throw new CustomApiException(ErrorCode.COMMON_UNAUTHORIZED);
        }
        return userId;
    }

    /**
     * 현재 로그인한 사용자의 로그인 ID(userLoginId) 반환 (로그 기록 용도)
     * @return userLoginId
     * @throws CustomApiException 인증되지 않았거나 값이 없는 경우
     */
    public static String currentLoginIdOrThrow() {
        String loginId = currentUserDetailsOrThrow().getUserLoginId();
        if (loginId == null || loginId.isEmpty()) {
            throw new CustomApiException(ErrorCode.COMMON_UNAUTHORIZED);
        }
        return loginId;
    }

    /**
     * 탈퇴/로그아웃 등에서 accessToken 원문 사용하기 위해 사용
     * - JwtAuthenticationFilter에서 request attribute에 심어둔 accessToken 꺼냄
     */
    public static String currentAccessTokenOrThrow(HttpServletRequest request) {
        Object tokenObj = request.getAttribute(SecurityTokenAttributes.ACCESS_TOKEN);
        if (!(tokenObj instanceof String token) || token.isBlank()) {
            throw new CustomApiException(ErrorCode.COMMON_UNAUTHORIZED);
        }
        return token;
    }
}
