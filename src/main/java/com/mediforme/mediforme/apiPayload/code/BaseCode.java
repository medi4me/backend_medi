package com.mediforme.mediforme.apiPayload.code;

import org.springframework.http.HttpStatus;

public interface BaseCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}

