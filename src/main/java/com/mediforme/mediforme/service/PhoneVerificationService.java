package com.mediforme.mediforme.service;

public interface PhoneVerificationService {
    void sendCode(String phone);
    void verify(String phone, String verificationCode);
}
