package com.mediforme.mediforme.global.advice;

import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.response.ApiResponse;
import com.mediforme.common.response.status.ErrorStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.format.DateTimeParseException;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    // 비즈니스 예외 (의도된 예외)
    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomApiException(CustomApiException e) {
        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(ApiResponse.onFailure(
                e.getErrorCode().getCode(),
                e.getErrorCode().getMessage()
            ));
    }

    // @Valid RequestBody 검증 실패
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException e,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request) {

        String message = Optional.ofNullable(e.getBindingResult().getFieldError())
            .map(fieldError -> fieldError.getDefaultMessage())
            .orElse("잘못된 요청입니다.");

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.onFailure(
                ErrorStatus._BAD_REQUEST.getCode(),
                message
            ));
    }

    // PathVariable 날짜 파싱 실패 (LocalDate.parse)
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiResponse<Object>> handleDateTimeParseException(DateTimeParseException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.onFailure(
                ErrorStatus._BAD_REQUEST.getCode(),
                "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"
            ));
    }

    // @Validated (PathVariable / RequestParam) 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(
        ConstraintViolationException e) {

        String message = e.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .findFirst()
            .orElse("잘못된 요청입니다.");

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.onFailure(
                ErrorStatus._BAD_REQUEST.getCode(),
                message
            ));
    }

    // 예상치 못한 시스템 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.onFailure(
                ErrorStatus._INTERNAL_SERVER_ERROR.getCode(),
                ErrorStatus._INTERNAL_SERVER_ERROR.getMessage()
            ));
    }
}
