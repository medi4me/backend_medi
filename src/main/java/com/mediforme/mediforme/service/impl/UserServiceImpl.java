package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.config.security.jwt.JwtToken;
import com.mediforme.mediforme.config.security.jwt.JwtTokenProvider;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.dto.request.UserLoginRequestDto;
import com.mediforme.mediforme.dto.request.UserRegisterRequestDto;
import com.mediforme.mediforme.dto.response.UserLoginResponseDto;
import com.mediforme.mediforme.dto.response.UserRegisterResponseDto;
import com.mediforme.mediforme.repository.UserRepository;
import com.mediforme.mediforme.service.AuthService;
import com.mediforme.mediforme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    // 사용자 로그인
    public UserLoginResponseDto login(UserLoginRequestDto.LoginRequestDto request){
        String userLoginId = request.getUserLoginId();
        String password = request.getPassword();

        // 인증 토큰 생성 및 검증
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLoginId, password);

        Authentication authentication =
                authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        String authenticationLoginId = authentication.getName();

        User user = userRepository.findByUserLoginId(authenticationLoginId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

        JwtToken jwtToken = jwtTokenProvider.generateToken(user.getUserId().toString());

        return UserLoginResponseDto.builder()
                .userLoginId(user.getUserLoginId())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }


    // 회원가입 (회원 저장 및 JWT 발급)
    @Override
    @Transactional
    public UserLoginResponseDto register(UserRegisterRequestDto.JoinRequest request){
        // 아이디 중복 체크
        userRepository.findByUserLoginId(request.getUserLoginId())
                .ifPresent(user -> {throw new CustomApiException(ErrorCode.DUPLICATED_USER_LOGIN_ID);});

        // 전화번호 중복 체크
        userRepository.findByPhone(request.getPhone())
                .ifPresent(user -> {throw new CustomApiException(ErrorCode.DUPLICATED_PHONE);});

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 엔티티 생성 및 저장
        User user = User.builder()
                .userLoginId(request.getUserLoginId())
                .userName(request.getUserName())
                .password(encodedPassword)
                .phone(request.getPhone())
                .consentCd(request.getConsentCd())
                .statusCd(1001L)    // ACTIVE 상태 공통코드
                .roleCd(1001L)      // ROLE_USER 기본값
                .build();
        User savedUser = userRepository.save(user);

        JwtToken jwtToken = authService.getToken(savedUser);                                // JWT 발급
        savedUser.updateRefreshToken(jwtToken.getRefreshToken(), savedUser.getUserId());    // Refresh Token 저장

        return UserLoginResponseDto.builder()
                .userLoginId(savedUser.getUserLoginId())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }


    // 회원 이름 조회
    @Override
    @Transactional(readOnly = true)
    public String findUserNameByLoginId(String userLoginId) {
        return userRepository.findByUserLoginId(userLoginId)
                .map(User::getUserName)
                .orElse("회원 이름을 찾을 수 없습니다.");
    }

    // 회원가입 결과 반환
    public UserRegisterResponseDto.JoinResultDTO buildJoinResult(User user) {
        return UserRegisterResponseDto.JoinResultDTO.builder()
                .userId(user.getUserId())
                .userLoginId(user.getUserLoginId())
                .userName(user.getUserName())
                .createdAt(LocalDateTime.now())
                .build();
    }
}