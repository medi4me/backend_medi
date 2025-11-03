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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * 사용자 로그인
     */
    @Override
    @Transactional(readOnly = true)
    public UserLoginResponseDto login(UserLoginRequestDto.LoginRequestDto request){
        String userLoginId = request.getUserLoginId();
        String password = request.getPassword();

        // Authentication 생성 및 검증
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLoginId, password);

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 사용자 조회
        User user = userRepository.findByUserLoginId(authentication.getName())
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

        // JWT 발급 (Access, Refresh Token)
        String accessToken = jwtTokenProvider.createAccessToken(userLoginId);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId(), userLoginId);

        JwtToken jwtToken = JwtToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return UserLoginResponseDto.builder()
                .userLoginId(userLoginId)
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }


    /**
     * 회원가입 (회원 저장 및 JWT 발급)
     */
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

        // Access + Refresh 발급 및 Redis 저장
        String accessToken = jwtTokenProvider.createAccessToken(savedUser.getUserLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser.getUserId(), savedUser.getUserLoginId());

        JwtToken jwtToken = JwtToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return UserLoginResponseDto.builder()
                .userLoginId(savedUser.getUserLoginId())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }


    /**
     * Refresh Token으로 Access Token 재발급
     */
    @Override
    @Transactional
    public JwtToken reissue(String refreshToken) {
        // Refresh Token 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomApiException(ErrorCode.EXPIRED_JWT_TOKEN);
        }

        // Redis에 존재하는 Refresh Token인지 확인
        String userLoginId = jwtTokenProvider.getUserLoginIdFromToken(refreshToken);
        User user = userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

        // Redis에 저장된 Refresh Token과 일치하는지 검증
        boolean tokenExists = jwtTokenProvider.isRefreshTokenValid(userLoginId, refreshToken);
        if (!tokenExists) {
            throw new CustomApiException(ErrorCode.INVALID_JWT_TOKEN);
        }

        // 새로운 Access Token 및 Refresh Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(userLoginId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId(), userLoginId);

        JwtToken jwtToken = JwtToken.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

        return jwtToken;
    }


    /**
     * JWT 발급 (내부용도)
     */
    @Override
    @Transactional
    public JwtToken getToken(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId(), user.getUserLoginId());
        return JwtToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 현재 로그인한 사용자의 로그인 ID 조회
     */
    @Override
    public String getLoginUserLoginId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


    /**
     * 현재 로그인 사용자 정보 조회
     */
    @Override
    public User getLoginUser() {
        String userLoginId = getLoginUserLoginId();
        return userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    }


    /**
     * 회원가입 결과 DTO 생성
     */
    public UserRegisterResponseDto.JoinResultDTO buildJoinResult(User user) {
        return UserRegisterResponseDto.JoinResultDTO.builder()
                .userId(user.getUserId())
                .userLoginId(user.getUserLoginId())
                .userName(user.getUserName())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
