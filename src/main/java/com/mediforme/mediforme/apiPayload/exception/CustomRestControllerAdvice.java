package com.mediforme.mediforme.apiPayload.exception;



import com.mediforme.mediforme.apiPayload.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class CustomRestControllerAdvice extends ResponseEntityExceptionHandler {
    /**
     * 커스텀 비지니스 예외 처리 (의미 있는 예외코드 그대로 내려줌)
     */
    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<Object> handleRestApiResponse(CustomApiException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     *  Bean Validation을 위반한 경우 (@NotBlank, @Pattern 등)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e) {
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Validation failed");

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.onFailure(errorCode.getCode(), errorMessage));
    }

    /**
     * @Valid RequestBody 유효성 검사 실패 에러 (DTO 필드 검증 실패)
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException e,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        String errorMessage = Optional.ofNullable(e.getBindingResult().getFieldError())
            .map(fe -> fe.getDefaultMessage())
            .orElse("Validation failed");

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.onFailure(errorCode.getCode(), errorMessage));
    }

    /**
     * DB 무결성/유니크 제약 위반 (중복 가입 등)
     *
     * Service에서 existsBy로 1차 차단해도 동시성 때문에 DB에서 터질 우려, 해당 예외를 500으로 뭉개면 원인 추적이 어려워 409로 변환
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolationException", e);

        // 회원가입 중복을 가장 흔한 케이스로 처리
        ErrorCode errorCode = ErrorCode.DUPLICATED_USER_LOGIN_ID;

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * 일반적인 서버 에러에 대한 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) {
        log.error("Unhandled exception", e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage()));
    }
}
