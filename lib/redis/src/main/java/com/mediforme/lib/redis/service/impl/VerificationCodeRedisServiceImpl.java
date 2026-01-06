package com.mediforme.lib.redis.service.impl;

import com.mediforme.lib.redis.entity.VerificationCode;
import com.mediforme.lib.redis.repository.VerificationCodeRedisRepository;
import com.mediforme.lib.redis.service.VerificationCodeRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationCodeRedisServiceImpl
        implements VerificationCodeRedisService {

    private static final long TTL_SECONDS = 180; // 3분

    private final VerificationCodeRedisRepository repository;

    @Override
    public void save(String phone, String code) {
        repository.save(
                VerificationCode.builder()
                        .phone(phone)
                        .code(code)
                        .ttl(TTL_SECONDS)
                        .build()
        );
    }

    @Override
    public boolean verify(String phone, String code) {
        return repository.findById(phone)
                .map(v -> v.getCode().equals(code))
                .orElse(false);
    }

    @Override
    public void delete(String phone) {
        repository.deleteById(phone);
    }
}