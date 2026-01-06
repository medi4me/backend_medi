package com.mediforme.lib.redis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Builder
@AllArgsConstructor
@RedisHash("verification_code")
public class VerificationCode {

    @Id
    private String phone;          // pk

    private String code;

    @TimeToLive
    private Long ttl;
}
