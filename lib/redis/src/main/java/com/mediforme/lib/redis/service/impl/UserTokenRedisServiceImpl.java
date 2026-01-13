package com.mediforme.lib.redis.service.impl;

import com.mediforme.lib.redis.entity.UserToken;
import com.mediforme.lib.redis.repository.UserTokenRedisRepository;
import com.mediforme.lib.redis.service.UserTokenRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTokenRedisServiceImpl implements UserTokenRedisService {

    private final UserTokenRedisRepository userTokenRedisRepository;

    /**
     * Refresh Token 저장
     */
    @Override
    public void saveUserToken(UserToken userToken) {
        userTokenRedisRepository.save(userToken);
        log.info("Redis 저장 완료 - userLoginId={}, refreshToken={}",
                userToken.getUserLoginId(), userToken.getRefreshToken());
    }

    /**
     * Refresh Token으로 조회
     */
    @Override
    public Optional<UserToken> findByRefreshToken(String refreshToken) {
        return userTokenRedisRepository.findById(refreshToken);
    }

    /**
     * userLoginId 기반 조회 (중복 로그인 제어)
     */
    @Override
    public Optional<UserToken> findByUserLoginId(String userLoginId) {
        return userTokenRedisRepository.findByUserLoginId(userLoginId);
    }


    /**
     * Refresh Token 삭제 (rotation)
     */
    @Override
    public void deleteByRefreshToken(String refreshToken) {
        userTokenRedisRepository.deleteById(refreshToken);
        log.info("Redis 삭제 완료 - refreshToken={}", refreshToken);
    }

    @Override
    public void deleteByUserLoginId(String userLoginId) {   // userLoginId로 삭제
        userTokenRedisRepository.findByUserLoginId(userLoginId)
            .ifPresent(token -> {
                userTokenRedisRepository.deleteById(token.getRefreshToken());
                log.info("Redis 삭제 완료 - userLoginId={}, refreshToken={}", userLoginId, token.getRefreshToken());
            });
    }

}
