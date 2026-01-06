package com.mediforme.mediforme.apiPayload.exception;

import lombok.Getter;

@Getter
public class CustomApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomApiException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 메시지 주입
        this.errorCode = errorCode;
    }

    public CustomApiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause); // cause까지 유지
        this.errorCode = errorCode;
    }
}
