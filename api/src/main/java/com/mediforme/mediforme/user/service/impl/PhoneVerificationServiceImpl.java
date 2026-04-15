package com.mediforme.mediforme.user.service.impl;

import com.mediforme.lib.redis.service.VerificationCodeRedisService;
import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.user.repository.UserRepository;
import com.mediforme.mediforme.user.service.PhoneVerificationService;
import com.mediforme.mediforme.global.util.SmsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class PhoneVerificationServiceImpl implements PhoneVerificationService {

    private final UserRepository userRepository;
    private final SmsUtil smsUtil;
    private final VerificationCodeRedisService verificationCodeRedisService;

    @Override
    public void sendCode(String phone) {

        if (userRepository.findByPhone(phone).isPresent()) {
            throw new CustomApiException(ErrorCode.DUPLICATED_PHONE);
        }

        String code = generateCode();
        smsUtil.sendOne(phone, code);

        verificationCodeRedisService.save(phone, code);
    }

    @Override
    public void verify(String phone, String verificationCode) {

        if (!verificationCodeRedisService.verify(phone, verificationCode)) {
            throw new CustomApiException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        verificationCodeRedisService.delete(phone);
    }

    private String generateCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}