package com.mediforme.mediforme.config.security.jwt;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.config.security.CustomUserDetails;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * username = userLoginId
     * 로그인 시 입력받은 사용자 ID로 DB 조회 수행
     */
    @Override
    public UserDetails loadUserByUsername(String userLoginId) {
        // 사용자 조회
        User user = userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userLoginId);
                    return new CustomApiException(ErrorCode.USER_NOT_FOUND);
                });

        return new CustomUserDetails(user);
    }
}
