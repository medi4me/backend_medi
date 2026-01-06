package com.mediforme.lib.redis.repository;

import com.mediforme.lib.redis.entity.VerificationCode;
import org.springframework.data.repository.CrudRepository;

public interface VerificationCodeRedisRepository
        extends CrudRepository<VerificationCode, String> {
}