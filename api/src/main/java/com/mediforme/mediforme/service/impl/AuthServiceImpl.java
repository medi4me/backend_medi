package com.mediforme.mediforme.service.impl;

import com.mediforme.lib.redis.entity.UserToken;
import com.mediforme.lib.redis.service.UserTokenRedisService;
import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.config.security.jwt.JwtToken;
import com.mediforme.mediforme.config.security.jwt.JwtTokenProvider;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.dto.request.UserLoginRequestDto;
import com.mediforme.mediforme.dto.request.UserRegisterRequestDto;
import com.mediforme.mediforme.dto.response.UserLoginResponseDto;
import com.mediforme.mediforme.repository.UserRepository;
import com.mediforme.mediforme.service.AuthService;
import com.mediforme.mediforme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final long REFRESH_TTL_SECONDS = 60L * 60 * 24 * 7; // 7일

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserTokenRedisService userTokenRedisService;
    private final UserService userService;

    /**
     * 로그인
     */
    @Override
    public UserLoginResponseDto login(UserLoginRequestDto.LoginRequestDto request){
        // Authentication 생성 및 검증
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUserLoginId(),
                    request.getPassword()
                )
            );
        } catch (DisabledException e) {
            // isEnabled() == false
            // 탈퇴/비활성 정책인 경우
            throw new CustomApiException(ErrorCode.USER_RESIGNED);
        } catch (LockedException e) {
            // isAccountNonLocked() == false
            throw new CustomApiException(ErrorCode.USER_RESIGNED);
        } catch (BadCredentialsException e) {
            // 비밀번호 틀림
            throw new CustomApiException(ErrorCode.INVALID_LOGIN);
        } catch (AuthenticationException e) {
            // 그 외 인증 실패
            throw new CustomApiException(ErrorCode.COMMON_UNAUTHORIZED);
        }

        // 인증 성공한 사용자만 도달
        User user = userService.getByLoginId(authentication.getName());

        JwtToken token = issueToken(user);

        userTokenRedisService.saveUserToken(
            UserToken.builder()
                .refreshToken(token.getRefreshToken())
                .userLoginId(user.getUserLoginId())
                .userId(user.getUserId())
                .role(String.valueOf(user.getRoleCd()))
                .ttlSeconds(REFRESH_TTL_SECONDS)
                .build()
        );

        return UserLoginResponseDto.builder()
            .userLoginId(user.getUserLoginId())
            .accessToken(token.getAccessToken())
            .refreshToken(token.getRefreshToken())
            .build();
    }


    /**
     * 회원가입 (회원 저장 및 JWT 발급)
     */
    @Override
    @Transactional
    public UserLoginResponseDto register(UserRegisterRequestDto.JoinRequest request){
        validateDuplicate(request);

        User user = userService.createUser(request);

        JwtToken token = issueToken(user);

        // refreshToken Redis 저장
        userTokenRedisService.saveUserToken(
            UserToken.builder()
                .refreshToken(token.getRefreshToken())
                .userLoginId(user.getUserLoginId())
                .userId(user.getUserId())
                .role(String.valueOf(user.getRoleCd()))
                .ttlSeconds(REFRESH_TTL_SECONDS)
                .build()
        );

        return UserLoginResponseDto.builder()
                .userLoginId(user.getUserLoginId())
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }


    /**
     * Refresh Token으로 Access Token 재발급
     */
    @Override
    @Transactional
    public JwtToken reissue(String refreshToken) {
        // Redis에서 refresh token 검증
        UserToken token = userTokenRedisService.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomApiException(ErrorCode.INVALID_JWT_TOKEN));

        // 탈퇴 사용자인 경우 재발급 금지
        User user = userService.getByLoginId(token.getUserLoginId());
        if (user.isResigned()){
            throw new CustomApiException(ErrorCode.USER_RESIGNED);
        }

        // Refresh Token Rotation (기존 토큰 즉시 폐기)
        userTokenRedisService.deleteByRefreshToken(refreshToken);

        // 새 토큰 발급
        String newAccess = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUserLoginId(), user.getRoleCd());
        String newRefresh = jwtTokenProvider.createRefreshToken(user.getUserId(), user.getUserLoginId(), user.getRoleCd());

        // Redis 저장
        userTokenRedisService.saveUserToken(
                UserToken.builder()
                    .refreshToken(newRefresh)
                    .userLoginId(user.getUserLoginId())
                    .userId(user.getUserId())
                    .role(String.valueOf(user.getRoleCd()))
                    .ttlSeconds(REFRESH_TTL_SECONDS)
                    .build()
        );

        return JwtToken.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .build();
    }


    /**
     * 토큰 발급 공통 로직 (DB User 기준으로 uid/role 포함해 발급)
     */
    private JwtToken issueToken(User user) {
        return JwtToken.builder()
                .accessToken(jwtTokenProvider.createAccessToken(
                        user.getUserId(),
                        user.getUserLoginId(),
                        user.getRoleCd()
                ))
                .refreshToken(jwtTokenProvider.createRefreshToken(
                        user.getUserId(),
                        user.getUserLoginId(),
                        user.getRoleCd()
                ))
                .build();
    }

    /**
     * 중복 검증
     */
    private void validateDuplicate(UserRegisterRequestDto.JoinRequest request) {

        if (userRepository.existsByUserLoginId(request.getUserLoginId())) {
            throw new CustomApiException(ErrorCode.DUPLICATED_USER_LOGIN_ID);
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new CustomApiException(ErrorCode.DUPLICATED_PHONE);
        }
    }


    @Override
    public void checkDuplicateLoginId(String userLoginId) {
        if (userRepository.existsByUserLoginId(userLoginId)) {
            throw new CustomApiException(ErrorCode.DUPLICATED_USER_LOGIN_ID);
        }
    }
}
