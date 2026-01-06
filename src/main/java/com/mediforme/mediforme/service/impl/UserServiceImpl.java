package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.dto.request.UserRegisterRequestDto;
import com.mediforme.mediforme.repository.UserRepository;
import com.mediforme.mediforme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 이름 조회
     */
    @Override
    public String findUserNameByLoginId(String userLoginId) {
        return userRepository.findByUserLoginId(userLoginId)
                .map(User::getUserName)
                .orElse("회원 이름을 찾을 수 없습니다.");
    }

    /**
     * 회원 생성
     */
    @Transactional
    @Override
    public User createUser(UserRegisterRequestDto.JoinRequest request){
        if (!Boolean.TRUE.equals(request.getAgreeToTerms())) {
            throw new CustomApiException(ErrorCode.CONSENT_REQUIRED);
        }
        User user = User.builder()
                .userLoginId(request.getUserLoginId())
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .statusCd(1001L)        // ACTIVE
                .roleCd(1001L)          // ROLE_USER
                .consentCd(1001L)       // CONSENT_Y
                .build();
        return userRepository.save(user);
    }

    /**
     * 로그인 ID 기준 사용자 조회
     */
    @Override
    public User getByLoginId(String userLoginId) {
        return userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    }
}