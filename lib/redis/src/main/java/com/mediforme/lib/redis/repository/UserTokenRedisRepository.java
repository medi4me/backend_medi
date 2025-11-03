package com.mediforme.lib.redis.repository;

import com.mediforme.lib.redis.entity.UserToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 토큰 Redis 저장소
 * - Spring Data Redis 가 자동으로 RedisHash 관리
 */
@Repository
public interface UserTokenRedisRepository extends CrudRepository<UserToken, String> {
    Optional<UserToken> findByUserLoginId(String userLoginId);
}
