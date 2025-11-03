package com.mediforme.mediforme.service;

public interface VerificationService {
    void sendCode(String phone);
    boolean verifyCode(String phone, String code);
    void removeCode(String phone);
    String generatePasswordResetToken(String phone);
    boolean validatePasswordResetToken(String phone, String token);
}