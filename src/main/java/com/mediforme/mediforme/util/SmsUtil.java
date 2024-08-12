package com.mediforme.mediforme.util;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import org.springframework.stereotype.Component;

@Component
@Getter
@Slf4j
public class SmsUtil {

    @Value("${spring.coolsms.api.key}")
    private String apiKey;

    @Value("${spring.coolsms.api.secret}")
    private String apiSecretKey;

    private DefaultMessageService messageService;

    @PostConstruct
    private void init(){
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecretKey, "https://api.coolsms.co.kr");
    }

    // 단일 메시지 발송 예제
    public SingleMessageSentResponse sendOne(String to, String verificationCode) {
        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("01025594304");
        message.setTo(to);
        message.setText("[MediforMe] 아래의 인증번호를 입력해주세요\n" + verificationCode);

        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println(response);
        return response;
    }
}
