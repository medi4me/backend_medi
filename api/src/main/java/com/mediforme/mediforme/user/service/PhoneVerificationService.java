package com.mediforme.mediforme.user.service;

public interface PhoneVerificationService {
    void sendCode(String phone);
    void verify(String phone, String verificationCode);
}
