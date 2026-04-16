package com.mediforme.mediforme.global.advice;

import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.format.DateTimeParseException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomRestControllerAdviceTest {

    private final CustomRestControllerAdvice advice = new CustomRestControllerAdvice();

    @Test
    @DisplayName("CustomApiException 은 ErrorCode 의 httpStatus 와 code 를 그대로 내려준다")
    void handleCustomApiException_returnsErrorCodeStatusAndCode() {
        CustomApiException e = new CustomApiException(ErrorCode.USER_NOT_FOUND);

        ResponseEntity<Object> response = advice.handleRestApiResponse(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getCode()).isEqualTo("USER401");
        assertThat(body.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("ConstraintViolationException 은 400 + 첫 위반 메시지")
    void handleConstraintViolationException_returns400WithFirstMessage() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("휴대폰 번호 형식이 올바르지 않습니다.");
        ConstraintViolationException e = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<Object> response = advice.handleConstraintViolationException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertThat(body.getCode()).isEqualTo("COMMON400");
        assertThat(body.getMessage()).isEqualTo("휴대폰 번호 형식이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 은 400 + 첫 FieldError 메시지")
    void handleMethodArgumentNotValid_returns400WithFirstFieldError() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("dto", "name", "필수 필드입니다.");
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        MethodArgumentNotValidException e = new MethodArgumentNotValidException(
            mock(MethodParameter.class), bindingResult);

        ResponseEntity<Object> response = advice.handleMethodArgumentNotValid(
            e, new HttpHeaders(), HttpStatusCode.valueOf(400), mock(WebRequest.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertThat(body.getCode()).isEqualTo("COMMON400");
        assertThat(body.getMessage()).isEqualTo("필수 필드입니다.");
    }

    @Test
    @DisplayName("DateTimeParseException 은 400 + 날짜 형식 안내 메시지 (PR #29 회귀 방지)")
    void handleDateTimeParseException_returns400WithDateFormatMessage() {
        DateTimeParseException e = new DateTimeParseException("invalid", "2025-13-45", 0);

        ResponseEntity<Object> response = advice.handleDateTimeParseException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertThat(body.getCode()).isEqualTo("COMMON400");
        assertThat(body.getMessage()).isEqualTo("날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)");
    }

    @Test
    @DisplayName("DataIntegrityViolationException 은 409 + 중복 로그인 ID 에러코드")
    void handleDataIntegrityViolation_returns409WithDuplicatedLoginIdCode() {
        DataIntegrityViolationException e = new DataIntegrityViolationException("duplicate");

        ResponseEntity<Object> response = advice.handleDataIntegrityViolation(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertThat(body.getCode()).isEqualTo("USER404");
    }

    @Test
    @DisplayName("처리되지 않은 Exception 은 500 + COMMON500")
    void handleException_returns500WithCommon500() {
        Exception e = new RuntimeException("unexpected");

        ResponseEntity<Object> response = advice.handleException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertThat(body.getCode()).isEqualTo("COMMON500");
    }
}
