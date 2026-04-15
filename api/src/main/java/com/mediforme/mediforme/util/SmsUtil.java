package com.mediforme.mediforme.util;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * (임시) CoolSMS 기반 문자 발송 유틸
 * - @PostConstruct 시점에 API Key로 초기화
 * - Redis TTL 기반 인증코드 전송에 사용
 * - 발신번호/수신번호는 반드시 010형식으로 입력해야 함
 */
@Component
@Getter
@Slf4j
public class SmsUtil {

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecretKey;

    @Value("${coolsms.api.sender}")
    private String senderPhone;

    private DefaultMessageService messageService;

    @PostConstruct
    private void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(
                apiKey,
                apiSecretKey,
                "https://api.coolsms.co.kr"
        );
        log.info("[SmsUtil] CoolSMS initialized with sender: {}", senderPhone);
    }


    /**
     * 단일 문자 발송
     * @param to 수신자 번호
     * @param verificationCode 인증번호
     */
    public SingleMessageSentResponse sendOne(String to, String verificationCode) {
        Message message = new Message();
        message.setFrom(senderPhone);
        message.setTo(to);
        message.setText("[Mediforme] 인증번호는 " + verificationCode + " 입니다.");

        try {
            SingleMessageSentResponse response = this.messageService
                    .sendOne(new SingleMessageSendingRequest(message));
            log.info("[SmsUtil] SMS sent successfully → to: {}, response: {}", to, response);
            return response;
        } catch (Exception e) {
            log.error("[SmsUtil] SMS send failed → to: {}, reason: {}", to, e.getMessage());
            throw e;
        }
    }
}

