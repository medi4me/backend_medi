package com.mediforme.lib.redis.repository;

import com.mediforme.lib.redis.entity.BlacklistToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * AccessToken 블랙리스트 Redis 저장소
 */
@Repository
public interface BlacklistRedisRepository extends CrudRepository<BlacklistToken, String> {
    boolean existsByAccessToken(String accessToken);
}
