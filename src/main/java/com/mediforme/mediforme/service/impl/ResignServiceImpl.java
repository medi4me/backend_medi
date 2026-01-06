package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.service.ResignService;
import com.mediforme.mediforme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResignServiceImpl implements ResignService {
    private final UserService userService;      // 현재 로그인 사용자 확인용

    // 회원 탈퇴 (논리적 삭제)
    @Transactional
    @Override
    public void resignUser() {
        // 현재 로그인한 사용자 정보 조회
        String loginId = currentLoginIdOrThrow();
        User loginUser = userService.getByLoginId(loginId);

        // 탈퇴 처리 - 상태코드만 변경 (실제 삭제는 X)
        loginUser.updateStatus(9999L, loginUser.getUserId());       // 탈퇴 상태 공통코드(9999L)
    }

    private String currentLoginIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new CustomApiException(ErrorCode.UNAUTHORIZED);
        }
        String name = auth.getName();
        if (name == null || "anonymousUser".equals(name)) {
            throw new CustomApiException(ErrorCode.UNAUTHORIZED);
        }
        return name;
    }
}
