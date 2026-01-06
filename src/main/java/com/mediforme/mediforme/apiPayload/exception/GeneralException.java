package com.mediforme.mediforme.apiPayload.exception;

import com.mediforme.mediforme.apiPayload.code.BaseCode;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final BaseCode errorCode;

    public GeneralException(BaseCode errorCode) {
        super(errorCode.getMessage());   // 메시지 일관성 확보
        this.errorCode = errorCode;
    }

    public GeneralException(BaseCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause); // 원인 예외 보존
        this.errorCode = errorCode;
    }
}
