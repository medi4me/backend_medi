package com.mediforme.mediforme.apiPayload.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common (시스템/요청 형식/기본 에러)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    COMMON_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    COMMON_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON402", "금지된 요청입니다."),
    UNAUTHORIZED_MODIFY(HttpStatus.BAD_REQUEST, "COMMON403", "수정, 삭제 권한이 없습니다."),
    USER_NOT_ADMIN(HttpStatus.UNAUTHORIZED, "COMMON403", "관리자만 사용 가능한 API입니다."),
    UNKNOWN_INQUIRY_TYPE(HttpStatus.BAD_REQUEST, "COMMON405", "알 수 없는 조회 타입입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER401", "사용자를 찾을 수 없습니다."),
    DUPLICATED_USER_NAME(HttpStatus.CONFLICT, "USER402", "이미 존재하는 사용자입니다."),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "USER403", "권한이 존재하지 않습니다."),
    DUPLICATED_USER_LOGIN_ID(HttpStatus.CONFLICT, "USER404", "이미 존재하는 사용자 로그인 아이디입니다."),
    DUPLICATED_PHONE(HttpStatus.CONFLICT, "USER405", "이미 존재하는 사용자 전화번호입니다."),
    CONSENT_REQUIRED(HttpStatus.BAD_REQUEST, "USER406", "약관 동의가 필요합니다."),
    USER_RESIGNED(HttpStatus.CONFLICT, "USER_403_001", "탈퇴한 계정입니다."),

    //Email
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "email401", "중복된 이메일이 존재합니다."),

    // JWT Token
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT401", "잘못된 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT402", "만료된 JWT 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT403", "지원하지 않는 JWT 토큰입니다."),
    EMPTY_JWT_CLAIMS(HttpStatus.UNAUTHORIZED, "JWT404", "JWT claims string이 비어있습니다."),
    UNAUTHORIZED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT405", "권한 정보가 없는 토큰입니다."),

    // Medicine
    MEDICINE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEDICINE401", "약물을 찾을 수 없습니다."),
    USER_MEDICINE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEDICINE402", "사용자 복용 약물을 찾을 수 없습니다."),

    // Authentication(인증 실패 전용) (401)
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH401", "인증이 필요합니다."),
    AUTH_INVALID_JWT(HttpStatus.UNAUTHORIZED, "AUTH402", "잘못된 JWT 토큰입니다."),
    AUTH_EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "AUTH403", "만료된 JWT 토큰입니다."),
    AUTH_UNSUPPORTED_JWT(HttpStatus.UNAUTHORIZED, "AUTH404", "지원하지 않는 JWT 토큰입니다."),
    AUTH_EMPTY_JWT_CLAIMS(HttpStatus.UNAUTHORIZED, "AUTH405", "JWT claims string이 비어있습니다."),

    // Login
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "AUTH406", "아이디 또는 비밀번호가 올바르지 않습니다."),

    // Authorized / Action (인가 실패(권한 없음) 전용)(403)
    UNAUTHORIZED_ACTION(HttpStatus.UNAUTHORIZED, "ACTION401", "접근 권한이 없습니다."),
    // Authorization (403)
    FORBIDDEN_ACTION(HttpStatus.FORBIDDEN, "ACTION403", "접근 권한이 없습니다."),


    // Status
    STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "STATUS404", "상태를 찾을 수 없습니다."),

    // Verification (SMS 인증 관련)
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "VERIFICATION401", "인증 코드를 너무 자주 요청했습니다. 잠시 후 다시 시도해주세요."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "VERIFICATION402", "인증 문자 발송에 실패했습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "VERIFICATION403", "인증 코드가 만료되었습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "VERIFICATION404", "인증 코드가 올바르지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
