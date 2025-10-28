package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.repository.UserRepository;
import com.mediforme.mediforme.service.AuthService;
import com.mediforme.mediforme.service.ResignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResignServiceImpl implements ResignService {
    private final UserRepository userRepository;
    private final AuthService authService;      // 현재 로그인 사용자 확인용

    // 회원 탈퇴 (논리적 삭제)
    @Transactional
    public void resignUser(String token) {
        // 현재 로그인한 사용자 정보 조회
        User loginUser = authService.getLoginUser();

        // 탈퇴 처리 - 상태코드만 변경 (실제 삭제는 X)
        loginUser.updateStatus(9999L, loginUser.getUserId());       // 탈퇴 상태 공통코드(9999L)
    }
}
