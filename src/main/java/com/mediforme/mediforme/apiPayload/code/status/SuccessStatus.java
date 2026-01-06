package com.mediforme.mediforme.apiPayload.code.status;

import com.mediforme.mediforme.apiPayload.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    // General Response
    _OK(HttpStatus.OK, "COMMON200", "성공입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}