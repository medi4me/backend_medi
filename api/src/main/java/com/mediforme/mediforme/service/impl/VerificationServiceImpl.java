package com.mediforme.mediforme.service.impl;

import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.service.VerificationService;
import com.mediforme.mediforme.util.SmsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 휴대폰 인증 코드 발송 및 검증 서비스
 * - Redis 기반으로 인증코드 저장 (TTL 3분)
 * - 중복 요청 제한 (60초 내 재발송 불가)
 * - 코드 검증 후 자동 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final StringRedisTemplate redisTemplate;
    private final SmsUtil smsUtil;

    private static final String PREFIX = "verification:";
    private static final String TIMESTAMP_PREFIX = "verification:timestamp:";
    private static final String RESET_PREFIX = "passwordReset:";
    private static final long TTL = 180L;   // 인증 코드 유효 시간 (3분)
    private static final long LIMIT_INTERVAL = 60L; // 재전송 제한 (60초)
    private static final long RESET_TTL = 300L; // 비밀번호 재설정 TTL 5분

    /**
     * 인증 코드 발송
     * - 60초 내 중복 요청 방지
     * - Redis TTL 자동 만료
     */
    @Override
    public void sendCode(String phone) {
        String lastRequestKey = TIMESTAMP_PREFIX + phone;
        String lastRequestTime = redisTemplate.opsForValue().get(lastRequestKey);

        // 60초 내 재요청 방지
        if (lastRequestTime != null) {
            throw new CustomApiException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 6자리 랜덤 코드 생성
        String code = String.format("%06d", new Random().nextInt(1_000_000));

        // SMS 발송
        try {
            smsUtil.sendOne(phone, code);
            log.info("[Verification] 인증 코드 발송 완료 → Phone: {}, Code: {}", phone, code);
        } catch (Exception e) {
            throw new CustomApiException(ErrorCode.SMS_SEND_FAILED);
        }

        // Redis 저장 (3분 TTL)
        redisTemplate.opsForValue().set(PREFIX + phone, code, TTL, TimeUnit.SECONDS);
        // 요청 제한 시간 기록 (60초)
        redisTemplate.opsForValue().set(lastRequestKey, "sent", LIMIT_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 인증 코드 검증
     * - 일치 시 Redis에서 코드 삭제
     * - 만료된 경우 예외 처리
     */
    @Override
    public boolean verifyCode(String phone, String code) {
        String key = PREFIX + phone;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new CustomApiException(ErrorCode.EXPIRED_VERIFICATION_CODE);
        }
        if (!savedCode.equals(code)) {
            throw new CustomApiException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        redisTemplate.delete(key); // 사용 후 삭제
        log.info("[Verification] 인증 코드 검증 성공 → Phone: {}", phone);
        return true;
    }

    /**
     * 인증 코드 강제 삭제 (관리자용)
     */
    @Override
    public void removeCode(String phone) {
        redisTemplate.delete(PREFIX + phone);
        redisTemplate.delete(TIMESTAMP_PREFIX + phone);
        log.info("[Verification] 인증 코드 삭제 → Phone: {}", phone);
    }

    /**
     * 비밀번호 재설정 토큰 발급
     */
    public String generatePasswordResetToken(String phone) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(RESET_PREFIX + phone, token, RESET_TTL, TimeUnit.SECONDS);
        return token;
    }

    /**
     * 비밀번호 재설정 토큰 검증
     */
    public boolean validatePasswordResetToken(String phone, String token) {
        String savedToken = redisTemplate.opsForValue().get(RESET_PREFIX + phone);
        if (savedToken == null) {
            throw new CustomApiException(ErrorCode.EXPIRED_VERIFICATION_CODE);
        }
        boolean valid = savedToken.equals(token);
        if (valid) redisTemplate.delete(RESET_PREFIX + phone);
        else throw new CustomApiException(ErrorCode.INVALID_VERIFICATION_CODE);
        return valid;
    }
}
