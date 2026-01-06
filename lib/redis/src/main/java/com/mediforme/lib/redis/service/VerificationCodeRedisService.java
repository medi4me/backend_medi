package com.mediforme.lib.redis.service;

public interface VerificationCodeRedisService {
    void save(String phone, String code);
    boolean verify(String phone, String code);
    void delete(String phone);
}
