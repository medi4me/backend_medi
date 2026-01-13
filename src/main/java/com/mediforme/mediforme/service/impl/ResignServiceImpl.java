package com.mediforme.mediforme.service.impl;

import com.mediforme.lib.redis.service.UserTokenRedisService;
import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.service.ResignService;
import com.mediforme.mediforme.service.TokenBlacklistService;
import com.mediforme.mediforme.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResignServiceImpl implements ResignService {
    private static final Long RESIGNED_STATUS_CD = 9999L; // 탈퇴 상태 공통코드

    private final UserService userService;      // 현재 로그인 사용자 확인용
    private final TokenBlacklistService tokenBlacklistService;
    private final UserTokenRedisService userTokenRedisService;

    // 회원 탈퇴 (논리적 삭제)
    @Transactional
    @Override
    public void resignUser(String accessToken) {
        // 현재 로그인 사용자 확인
        String loginId = currentLoginIdOrThrow();
        User loginUser = userService.getByLoginId(loginId);

        // AccessToken이 없는 경우: 탈퇴했는데 토큰 살아있는 위험 상태 방지
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("[RESIGN] abort: empty accessToken");
            throw new CustomApiException(ErrorCode.COMMON_UNAUTHORIZED);
        }

        // refreshToken 폐기 (재발급 차단)
        userTokenRedisService.deleteByUserLoginId(loginId);

        // accessToken은 즉시 무효화 (재호출/재사용 방지)
        tokenBlacklistService.addToBlacklist(accessToken);

        // 이미 탈퇴 상태면 DB 상태 변경만 멱등 처리
        if(loginUser.isResigned()) {
            log.info("[RESIGN] idempotent: already resigned. loginId={}, userId={}", loginId, loginUser.getUserId());
            return;
        }

        // 논리적 탈퇴 처리 - 상태코드만 변경
        loginUser.updateStatus(RESIGNED_STATUS_CD, loginUser.getUserId());

        //감사 로그 (토큰 원문 기록 X)
        log.info("[RESIGN] success. loginId={}, userId={}", loginId, loginUser.getUserId());
    }

    private String currentLoginIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Spring Security 인증 객체가 없거나 인증되지 않은 경우
        if (auth == null || !auth.isAuthenticated()) {
            throw new CustomApiException(ErrorCode.COMMON_UNAUTHORIZED);
        }

        String name = auth.getName();

        // 익명 사용자 방어
        if (name == null || "anonymousUser".equals(name)) {
            throw new CustomApiException(ErrorCode.COMMON_UNAUTHORIZED);
        }

        return name;
    }
}
