package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.dto.request.UserRegisterRequestDto;
import com.mediforme.mediforme.dto.response.UserAdminResponseDto;
import com.mediforme.mediforme.dto.response.UserMeResponseDto;
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

    private static final long STATUS_ACTIVE = 2001L;
    private static final long ROLE_USER = 1001L;
    private static final long CONSENT_AGREED = 1L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 이름 조회 (loginId 기반)
     */
    @Override
    public String findUserNameByLoginId(String userLoginId) {
        User user = userRepository.findByUserLoginId(userLoginId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
        return user.getUserName();
    }

    /**
     * 회원 생성
     * - 기본 상태/권한/동의 코드는 서버에서 강제 세팅
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
                .statusCd(STATUS_ACTIVE)
                .roleCd(ROLE_USER)
                .consentCd(CONSENT_AGREED)
                .build();
        return userRepository.save(user);
    }

    /**
     * loginId 기준 사용자 조회 (인증/로그인 사용)
     */
    @Override
    public User getByLoginId(String userLoginId) {
        return userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 내 정보 조회 (userId 기반)
     */
    @Override
    public UserMeResponseDto getMe(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

        return UserMeResponseDto.builder()
            .userId(user.getUserId())
            .userLoginId(user.getUserLoginId())
            .userName(user.getUserName())
            .phone(user.getPhone())
            .roleCd(user.getRoleCd())
            .consentCd(user.getConsentCd())
            .statusCd(user.getStatusCd())
            .build();
    }

    /**
     * 관리자/매니저용 사용자 조회
     */
    @Override
    public UserAdminResponseDto getUserForAdmin(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

        return UserAdminResponseDto.builder()
            .userId(user.getUserId())
            .userLoginId(user.getUserLoginId())
            .userName(user.getUserName())
            .phone(user.getPhone())
            .roleCd(user.getRoleCd())
            .consentCd(user.getConsentCd())
            .statusCd(user.getStatusCd())
            .build();
    }
}