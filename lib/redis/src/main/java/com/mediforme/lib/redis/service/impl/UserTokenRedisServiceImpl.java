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

    @Override
    public void saveUserToken(UserToken userToken) {
        userTokenRedisRepository.save(userToken);
        log.info("Redis 저장 완료 - userLoginId={}, accessToken={}",
                userToken.getUserLoginId(), userToken.getAccessToken());
    }

    @Override
    public Optional<UserToken> findByAccessToken(String accessToken) {
        return userTokenRedisRepository.findById(accessToken);
    }

    @Override
    public Optional<UserToken> findByUserLoginId(String userLoginId) {
        return userTokenRedisRepository.findByUserLoginId(userLoginId);
    }

    @Override
    public void deleteByAccessToken(String accessToken) {
        userTokenRedisRepository.deleteById(accessToken);
        log.info("Redis 삭제 완료 - accessToken={}", accessToken);
    }
}
